#!/bin/bash
# Creates a CA

openssl req -new -x509 -days 365 -keyout private/cakey.pem -out cacert.pem -config /etc/ssl/openssl.cnf
