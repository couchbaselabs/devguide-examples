#!/bin/bash
set -x
set -e

# generate an RSA private key
openssl genrsa -out private.key 2048

# optionally remove the password
# openssl rsa -in private.key -out private-nokey.pem

# generate a new x509 certificate from the private key
openssl req -new -x509 -key private.key -out publickey.cer -days 365

# export the private key and the x509 certificate into a .pfx uisng a password:
openssl pkcs12 -export -out public_privatekey.pfx -inkey private.key -in publickey.cer
