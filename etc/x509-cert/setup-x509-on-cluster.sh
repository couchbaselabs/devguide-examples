#!/bin/sh -xe

export CB_ROOT=/opt/couchbase
# export CB_ROOT=${HOME}/code/couchbase/server/install

# Create environment variables for the naming of a directory-structure, within
# which will reside the certificates you create for root, intermediate, and
# node.
#
# Note that in cases where multiple intermediate and/or node certificates are
# to be included in the certificate-chain, multiple intermediate and/or
# directories are required — one for each intermediate or certificate. 
export TOPDIR=SSLCA
export ROOT_DIR=rootdir
export NODE_DIR=nodedir
export INT_DIR=intdir

# Create environment variables for each of the certificate-files to be created.
#
# Note that in cases where multiple intermediate and/or node certificates are
# to be included in the certificate-chain, additional environment-variable
# definitions — one for each of the additional intermediate and/or node
# certificates — are required. 
export ROOT_CA=ca
export INTERMEDIATE=int
export NODE=pkey
export CHAIN=chain

# Create environment variables for the administrator-credentials to be used for
# certificate-management, the IP address at which the Couchbase Server-node is
# located, and the username required for role-based access to a particular
# resource. 
#
# Note that in this example, the username is specified as travel-sample, which
# is typically associated with the Bucket Full Access role, on the bucket
# travel-sample. For access to be fully tested, ensure that the travel-sample
# user has indeed been defined on the Couchbase Server-node, and is associated
# with the Bucket Full Access role. (See Authorization for more information on
# RBAC.) 
export ADMINNAME=Administrator
export ADMINPASS=password
export ADMINCRED=${ADMINNAME}:${ADMINPASS}
export ip=127.0.0.1
export USERNAME=testuser

# Create a directory-structure in which, within a top-level directory named
# SSLCA, three subdirectories reside — rootdir, intdir, and nodedir —
# respectively to hold the certificates you create for root, intermediate, and
# node. 
mkdir -p ${TOPDIR}
cd ${TOPDIR}
mkdir -p ${ROOT_DIR}
mkdir -p ${INT_DIR}
mkdir -p ${NODE_DIR}

# Generate the root private key file (ca.key) and the public key file (ca.pem):
cd ${ROOT_DIR}
openssl genrsa -out ${ROOT_CA}.key 2048
openssl req -new -x509 -days 3650 -sha256 -key ${ROOT_CA}.key \
  -out ${ROOT_CA}.pem -subj '/C=UA/O=MyCompany/CN=MyCompanyRootCA'

# Generate, first, the intermediate private key (int.key); and secondly, the
# intermediate certificate signing-request (int.csr):
cd ../${INT_DIR}
openssl genrsa -out ${INTERMEDIATE}.key 2048
openssl req -new -key ${INTERMEDIATE}.key -out ${INTERMEDIATE}.csr \
  -subj '/C=UA/O=MyCompany/CN=MyCompanyIntermediateCA'

# Create the extension file v3_ca.ext; in order to add extensions to the
# certificate, and to generate the certificate signing-request: 
cat <<EOF>> ../v3_ca.ext
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer:always
basicConstraints = CA:true
EOF

# Create open ssl configuration file to add Subject Alternative Names
# Add the server IP or DOMAIN in "alt_names"
cat <<EOF>> ../openssl.cnf
[req]
x509_extensions = v3_req
distinguished_name = req_distinguished_name

[req_distinguished_name]

[ v3_req ]

# Extensions to add to a certificate request

basicConstraints = CA:true
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
IP.1 = ${ip}
IP.2 = 172.23.120.17
EOF

# Generate the intermediate public key (int.pem), based on the intermediate
# certificate signing-request (int.csr), and signed by the root public key
# (ca.pem). 
openssl x509 -req -in ${INTERMEDIATE}.csr \
  -CA ../${ROOT_DIR}/${ROOT_CA}.pem -CAkey ../${ROOT_DIR}/${ROOT_CA}.key \
  -CAcreateserial -CAserial ../${ROOT_DIR}/rootCA.srl -extfile ../v3_ca.ext \
  -out ${INTERMEDIATE}.pem -days 365

# Generate, first, the node private key (pkey.key); secondly, the node
# certificate signing-request (pkey.csr); and thirdly, the node public key
# (pkey.pem). 
cd ../${NODE_DIR}
openssl genrsa -out ${NODE}.key 2048
openssl req -new -key ${NODE}.key -out ${NODE}.csr \
  -subj "/C=UA/O=MyCompany/CN=${USERNAME}"
openssl x509 -req -in ${NODE}.csr -CA ../${INT_DIR}/${INTERMEDIATE}.pem \
  -CAkey ../${INT_DIR}/${INTERMEDIATE}.key -CAcreateserial \
  -CAserial ../${INT_DIR}/intermediateCA.srl -out ${NODE}.pem -days 365 -extfile ../openssl.cnf -extensions 'v3_req'

# Generate the certificate chain-file, by concatenating the node and
# intermediate certificates. This allows the client to verify the intermediate
# certificate against the root certificate.
#
# Note that if multiple intermediate certificates are specified for
# concatenation in this way, the concatenation-order must correspond to the
# order of signing. Thus, the node certificate, which appears in the first
# position, has been signed by the intermediate certificate, which therefore
# appears in the second position: and in cases where this intermediate
# certificate has itself been signed by a second intermediate certificate, the
# second intermediate certificate must appear in the third position, and so on.
#
# Note also that the root certificate is never included in the chain. 
cd ..
cat ./${NODE_DIR}/${NODE}.pem ./${INT_DIR}/${INTERMEDIATE}.pem > ${CHAIN}.pem

# Manually copy the node private key (pkey.key) and the chain file (chain.pem)
# to the inbox folder of the Couchbase Server-node: 
mkdir -p $CB_ROOT/var/lib/couchbase/inbox/
cp ./${CHAIN}.pem $CB_ROOT/var/lib/couchbase/inbox/${CHAIN}.pem
chmod a+x $CB_ROOT/var/lib/couchbase/inbox/${CHAIN}.pem
cp ./${NODE_DIR}/${NODE}.key $CB_ROOT/var/lib/couchbase/inbox/${NODE}.key
chmod a+x $CB_ROOT/var/lib/couchbase/inbox/${NODE}.key

# Upload the root certificate, and activate it: 
curl -X POST --data-binary "@./${ROOT_DIR}/${ROOT_CA}.pem" \
http://${ADMINCRED}@${ip}:8091/controller/uploadClusterCA
curl -X POST http://${ADMINCRED}@${ip}:8091/node/controller/reloadCertificate


$CB_ROOT/bin/couchbase-cli ssl-manage -c ${ip}:8091 -u ${ADMINNAME} -p ${ADMINPASS} \
  --upload-cluster-ca=./${ROOT_DIR}/${ROOT_CA}.pem
$CB_ROOT/bin/couchbase-cli ssl-manage -c ${ip}:8091 -u ${ADMINNAME} -p ${ADMINPASS} \
  --set-node-certificate

# Enable Client Certificate
cat <<EOF>> conf.json
{"state": "enable","prefixes": [{"path": "subject.cn","prefix": "","delimiter": ""}]}
EOF

$CB_ROOT/bin/couchbase-cli ssl-manage -c ${ip}:8091 -u ${ADMINNAME} -p ${ADMINPASS} \
  --set-client-auth conf.json

