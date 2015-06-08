=========================================
Setting up Sentinel with NGINX and Docker
=========================================


TODO: insert header

NGINX Docker configuration
--------------------------

Follow the instructions at `DockerHub Nginx image`_; create `Dockerfile`::

    FROM nginx
    COPY public /usr/share/nginx/html
    COPY conf/nginx.conf /etc/nginx/nginx.conf

this is the ``public/Dockerfile`` file, used to build and run the proxy container::

    $ docker build -t sentinel ./public
    $ docker run --name sentinel-proxy -p 80:80 -d sentinel

at this point you can hit the `http://docker-host-ip/` endpoint and see the login screen.


Mongo Docker
------------

Follow the instructions at `DockerHub Mongo image`_::

    $ docker run --name sentinel-mongo -p 27017:27017 -d mongo mongod --smallfiles

this exposes port `27017` so we can connect directly to it.

API Server (sbt)
----------------

This is done via the ``sentinel.Dockerfile`` docker build.

To use this Dockerfile, you must first run `sbt docker:stage`
then copy that file into ``target/docker``, renaming it ``Dockerfile``::

    $ rm target/docker/Dockerfile
    $ cp build/sentinel.Dockerfile target/docker/Dockerfile

Build the docker image::

    $ docker build -t massenz/sentinel .

and then run the container::

    $ docker run -p 80:9000 -d massenz/sentinel

The REST API will be exposed on the docker host on port 80 (default HTTP).


Connecting to Container
-----------------------

To SSH into the running container use::

    $ docker exec -i -t [name] bash

replace `[name]` with the container's name.



Links and notes
---------------


.. _DockerHub Nginx image: https://registry.hub.docker.com/_/nginx/
.. _DockerHub Mongo image: https://registry.hub.docker.com/_/mongo/
