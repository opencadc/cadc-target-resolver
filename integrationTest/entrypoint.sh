#!/bin/sh

echo "${SSL_CERT}" > /etc/nginx/server.crt
echo "${SSL_KEY}" > /etc/nginx/server.key


nginx -g "daemon off;"
