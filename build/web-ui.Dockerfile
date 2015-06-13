# Created MM, 2015-05-31
#
# Builds a docker container running Nginx with all the
# static files for the Sentinel UI

# To use this Dockerfile, use build-web-proxy.py

FROM nginx
ADD sentinel-webui.tar.gz /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
