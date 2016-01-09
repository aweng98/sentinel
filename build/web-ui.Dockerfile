# Created MM, 2015-05-31
#
# Builds a docker container running Nginx with all the
# static files for the Sentinel UI

# To use this Dockerfile, use build-web-proxy.py

FROM nginx
MAINTAINER Marco Massenzio <marco@alertavert.com>

ADD sentinel-webui.tar.gz /usr/share/nginx/html/web

COPY nginx.conf /etc/nginx/nginx.conf

# Self-signed certificates for development only.
#
# Created with: $ sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout build/nginx.key -out build/nginx.crt
# See: https://www.digitalocean.com/community/tutorials/how-to-create-an-ssl-certificate-on-nginx-for-ubuntu-14-04
COPY nginx.crt /etc/nginx/sentinel.crt
COPY nginx.key /etc/nginx/sentinel.key
