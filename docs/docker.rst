=========================================
Setting up Sentinel with NGINX and Docker
=========================================


TODO: insert header

NGINX Docker configuration
--------------------------

Follow the instructions at `DockerHub Nginx image`_; create `Dockerfile`::

    FROM nginx
    COPY sentinel/public /usr/share/nginx/html
    COPY sentinel/conf/nginx.conf /etc/nginx/nginx.conf

TODO: create the nginx.conf file in sentinel

then build and run the proxy container::

    $ docker build -t sentinel .
    $ docker run --name sentinel-proxy -p 80:80 -d sentinel

at this point you can hit the `http://docker-host-ip/` endpoint and see the login screen.


Mongo Docker
------------

Follow the instructions at `DockerHub Mongo image`_::

    $ docker run --name sentinel-mongo -p 27017:27017 -d mongo mongod --smallfiles

this exposes port `27017` so we can connect directly to it.

API Server (sbt)
----------------


Connecting to Container
-----------------------

To SSH into the running container use::

    $ docker exec -i -t [name] bash

replace `[name]` with the container's name.



Links and notes
---------------


.. _DockerHub Nginx image: https://registry.hub.docker.com/_/nginx/
.. _DockerHub Mongo image: https://registry.hub.docker.com/_/mongo/