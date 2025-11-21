#!/bin/bash

KCHOST=http://localhost:8080
REALM=ingredient-scan
CLIENT_ID=ingredient-scan-service
UNAME=iuser
PASSWORD=otus

ACCESS_TOKEN=`curl \
  -d "client_id=$CLIENT_ID" \
  -d "username=$UNAME" \
  -d "password=$PASSWORD" \
  -d "grant_type=password" \
  "$KCHOST/realms/$REALM/protocol/openid-connect/token" | jq -r '.access_token'`
echo "$ACCESS_TOKEN"
