#!/bin/bash
# Convert PEM to CERT

openssl x509 -inform PEM -in cacert.pem -outform DER -out certificate.cer
