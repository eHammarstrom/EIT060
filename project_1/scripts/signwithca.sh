#!/bin/bash
# Sign a specific certificate with our CA keys

read -p "Provide certificate name: " certName

openssl x509 -req -days 360 -in $certName.csr -CA public/ca.crt -CAkey private/ca.key -CAcreateserial -out public/$certName.crt
