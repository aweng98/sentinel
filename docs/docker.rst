=========================================
Setting up Sentinel with NGINX and Docker
=========================================


TODO: insert header


Mongo Docker
------------

Follow the instructions at `DockerHub Mongo image`_::

    $ docker run --name sentinel-mongo -p 27017:27017 -d mongo mongod --smallfiles

this exposes port `27017` so we can connect directly to it.


API Server (sbt)
----------------

TODO: all this must be automated, like in build-web-proxy.py (see #96950418)

This is done via the ``sentinel.Dockerfile`` docker build.

To use this Dockerfile, you must first copy the ``override.conf`` into the ``conf/`` directory::

    $ cp build/override.conf conf/

(this seems to be a limitation in the sbt docker plugin) then run::

    $ sbt docker:stage

The ``Dockerfile`` this generates is all wrong, instead use the one in ``build/``::

    $ cp build/sentinel.Dockerfile target/docker/Dockerfile

**NOTE** Remember to remove ``override.conf`` from the ``conf/`` directory, or unit tests will fail.

Build the docker image::

    $ docker build -t massenz/sentinel .

and then run the container::

    $ docker run --name sentinel-app -p 9000:9000 -d massenz/sentinel

The REST API will be exposed on the docker host on port 9000 (if you change this,
remember to update the Nginx proxy configuration).


NGINX Docker configuration
--------------------------

Follow the instructions at `DockerHub Nginx image`_; create `Dockerfile`; or, alternatively,
use the script in ``build/build-web-proxy.py`` (use with ``--help`` to view options).

Once the Docker image is built, it can be run with::

    $ docker run --name sentinel-proxy -p 80:80 -d massenz/sentinel-nginx

at this point you can hit the `http://docker-host-ip/` endpoint and see the login screen.


Connecting to Container
-----------------------

To SSH into the running container use::

    $ docker exec -i -t [name] bash

replace `[name]` with the container's name.


Starting the Application
------------------------

Once all the Docker images have been created, they can be started, in order, with the
following commands::

    docker run --name sentinel-mongo -d  -p 27017:27017 mongo
    docker run --name sentinel -d  -p 9000:9000 massenz/sentinel
    docker run --name sentinel-web -d -i -p 80:80 massenz/sentinel-nginx


Links and notes
---------------


.. _DockerHub Nginx image: https://registry.hub.docker.com/_/nginx/
.. _DockerHub Mongo image: https://registry.hub.docker.com/_/mongo/
