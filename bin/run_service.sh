#!/bin/bash

# Docker Machine-based build of Sentinel API Service
#
# Currently tested only on OSX

if [[ -z $(docker-machine ls | grep sentinel) ]]; then
  echo "No Sentinel Docker Machine found; creating a brand new one"
  docker-machine create -d vmwarefusion sentinel
fi  
eval $(docker-machine env sentinel)

# TODO: this assumes virtualenv and a specific one exists.
workon sentinel

./bin/build-web-proxy.py 
./bin/build-api-server.py 

# TODO: stop and remove running containers

docker run --name mongo-dev -d mongo
docker run --name sentinel-api -p 9000:9000 -d --link mongo-dev massenz/sentinel-apiserver 
# TODO: note the name of the API server is hard-coded in the nginx configuration
#   and is now different from the name of the container. We need to parametize this.
docker run --link sentinel-api:sentinel-dev --name sentinel-ui -d -p 8080:80 massenz/sentinel-nginx

echo "The API server is now available at http://$(docker-machine ip sentinel):9000"
echo "The UI can be seen from the browser at: http://$(docker-machine ip sentinel):8080/web/"
