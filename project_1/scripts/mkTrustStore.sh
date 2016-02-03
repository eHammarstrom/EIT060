#!/bin/bash
# Creates a truststore

keytool -import -file certificate.cer -alias servertruststore -keystore servertruststore
