# Created MM, 2015-05-31
#
# Builds a docker container running Nginx with all the
# static files for the Sentinel UI

# To use this Dockerfile, use build-web-proxy.py

FROM nginx
COPY public /usr/share/nginx/html
COPY conf/nginx.conf /etc/nginx/nginx.conf
