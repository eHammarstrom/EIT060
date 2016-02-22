#!/bin/bash

read -p "Keystore name: " keystoreName

keytool -keystore $keystoreName -genkey -alias $keystoreName

keytool -keystore $keystoreName -certreq -alias $keystoreName -keyalg rsa -file $keystoreName.csr

openssl x509 -req -CA ca/cacert.pem -CAkey ca/private/cakey.pem -in $keystoreName.csr -out $keystoreName\_cert.cer -days 365 -CAcreateserial

keytool -import -keystore $keystoreName -file ca/cacert.pem -alias cert

keytool -import -keystore $keystoreName -file $keystoreName\_cert.cer -alias $keystoreName
