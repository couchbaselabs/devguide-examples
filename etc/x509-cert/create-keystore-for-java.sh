#!/bin/sh -xe

# Create keystore file and import ROOT/INTERMEDIATE/CLIENT cert
# This is specific to Java SDK

export ROOT_CA=ca
export INTERMEDIATE=int
export TOPDIR=SSLCA
export ROOT_DIR=rootdir
export INT_DIR=intdir
export KEYSTORE_DIR=keystore

export USERNAME=testuser
export KEYSTORE_FILE=keystore.jks
export STOREPASS=123456


cd ${TOPDIR}
mkdir -p ${KEYSTORE_DIR}
cd ${KEYSTORE_DIR}

keytool -genkey -keyalg RSA -alias selfsigned -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -validity 360 -keysize 2048 -noprompt \
-dname "CN=${USERNAME}, OU=None, O=None, L=None, S=None, C=US" \
-keypass ${STOREPASS} -storetype pkcs12

keytool -certreq -alias selfsigned -keyalg RSA -file my.csr -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt -storetype pkcs12
openssl x509 -req -in my.csr -CA ../${INT_DIR}/${INTERMEDIATE}.pem -CAkey ../${INT_DIR}/${INTERMEDIATE}.key -CAcreateserial -out clientcert.pem -days 365

# Add ROOT CA
keytool -import -trustcacerts -file ../${ROOT_DIR}/${ROOT_CA}.pem -alias root -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt -storetype pkcs12
# Add Intermediate
keytool -import -trustcacerts -file ../${INT_DIR}/${INTERMEDIATE}.pem -alias int -keystore ${KEYSTORE_FILE} -storepass ${STOREPASS} -noprompt -storetype pkcs12
keytool -import -keystore ${KEYSTORE_FILE} -file clientcert.pem -alias selfsigned -storepass ${STOREPASS} -noprompt -storetype pkcs12
