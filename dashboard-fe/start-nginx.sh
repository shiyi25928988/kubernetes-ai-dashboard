#!/bin/sh
set -e

# Substitute environment variables in nginx config template
envsubst '${BACKEND_HOST},${BACKEND_PORT}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Execute the original entrypoint
nginx -g 'daemon off;'
