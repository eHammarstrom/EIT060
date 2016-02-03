#!/bin/bash
# Sign cert with csr

openssl x509 -req -CA cacert.pem -CAkey ~/SSL/private/cakey.pem -in client.csr -out clientcert.cer -days 365 -CAcreateserial
