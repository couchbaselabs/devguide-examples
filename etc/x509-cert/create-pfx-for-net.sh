#!/bin/sh -xe

export PFX_DIR=pfx
export TOPDIR=SSLCA
export CLIENT_DIR=clientdir

export CLIENT=client
export CHAIN=chain



cd ${TOPDIR}
mkdir -p ${PFX_DIR}
cd ${PFX_DIR}

openssl pkcs12 -export -inkey ../${ROOT_DIR}/${ROOT_CA}.key -inkey ../${INT_DIR}/${INTERMEDIATE}.key -inkey ../${CLIENT_DIR}/${CLIENT}.key -in ../${CLIENT_DIR}/chain.pem -out client.pfx

#On windows, double click on pfx file and Install pfx file on Local Machine section. Location should be "Trusted Root Certification Authorities"
