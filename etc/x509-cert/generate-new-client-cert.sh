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
export CLIENT_DIR=clientdir
export INT_DIR=intdir

# Create environment variables for each of the certificate-files to be created.
#
# Note that in cases where multiple intermediate and/or node certificates are
# to be included in the certificate-chain, additional environment-variable
# definitions — one for each of the additional intermediate and/or node
# certificates — are required. 
export ROOT_CA=ca
export INTERMEDIATE=int
export CLIENT=client
export CHAIN=chain
export TRUST=trust

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
export ADMINCRED=Administrator:password
export ip=127.0.0.1
export USERNAME=testuser

cd ${TOPDIR}

mkdir -p ${CLIENT_DIR}

cd ${CLIENT_DIR}
openssl genrsa -out ${CLIENT}.key 2048
openssl req -new -key ${CLIENT}.key -out ${CLIENT}.csr -subj "/CN=${USERNAME}/OU=None/O=None/L=None/S=None/C=US"
openssl x509 -req -in ${CLIENT}.csr -CA ../${INT_DIR}/${INTERMEDIATE}.pem \
  -CAkey ../${INT_DIR}/${INTERMEDIATE}.key -CAcreateserial \
  -CAserial ../${INT_DIR}/intermediateCA.srl -out ${CLIENT}.pem -days 365 -extfile ../openssl.cnf -extensions 'v3_req'

cat ../${INT_DIR}/${INTERMEDIATE}.pem ../${ROOT_DIR}/${ROOT_CA}.pem > ./${TRUST}.pem
cat ./${CLIENT}.pem ../${INT_DIR}/${INTERMEDIATE}.pem ../${ROOT_DIR}/${ROOT_CA}.pem > ./${CHAIN}.pem

set +x
echo "ROOT CA: $(realpath ../${ROOT_DIR}/${ROOT_CA}.pem)"
echo "INTERMEDIATE CA: $(realpath ../${INT_DIR}/${INTERMEDIATE}.pem)"
echo "TRUSTSTORE CA (ROOT+INTERMEDIATE): $(realpath ./${TRUST}.pem)"
echo "CHAINED: $(realpath ./${CHAIN}.pem)"
echo "CLIENT CERT: $(realpath ./${CLIENT}.pem)"
