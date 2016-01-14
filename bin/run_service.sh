#!/bin/bash

# Docker Machine-based build of Sentinel API Service
#
# Currently tested only on OSX


if [[ -z $(docker-machine ls | grep sentinel) ]]; then
  echo "No Sentinel Docker Machine found; creating a brand new one"
  docker-machine create -d vmwarefusion sentinel
fi  
eval $(docker-machine env sentinel)
DOCKER_IP=$(docker-machine ip sentinel)

if [[ -z ${DOCKER_IP} ]]; then
  echo "ERROR - the Sentinel Docker Machine could not be activated; aborting."
  exit 1
fi

echo "The 'sentinel' Docker Machine is now active, and can be reached at ${DOCKER_IP};
to SSH into it use 'docker-machine ssh sentinel'"

./bin/build-web-proxy.py
RES=$?

# Override settings copied to where they will be picked during image build:
if [[ -f ~/.sentinel/override.conf ]]; then
  cp ~/.sentinel/override.conf ./conf/
fi
./bin/build-api-server.py
rm -f ./conf/override.conf


if [[ $? != 0 || ${RES} != 0 ]]; then
  echo "ERROR - Could not build containers"
  exit 1
fi

for ctr in sentinel-ui sentinel-api mongo-dev; do
  NAME=$(docker rm $(docker stop $ctr))
  if [[ $? != 0 ]]; then
    echo "ERROR - could not stop and remove $ctr."
    exit 1
  fi
  echo "Container ${NAME} stopped and removed"
done

# TODO: create a data-only container, for the data we want to preserve across runs.

docker run --name mongo-dev -p 27017:27017 -d mongo
docker run --name sentinel-api -p 9000:9000 -d --link mongo-dev massenz/sentinel-apiserver 
# TODO: note the name of the API server is hard-coded in the nginx configuration
#   and is now different from the name of the container. We need to parametize this.
docker run --link sentinel-api:sentinel-dev --name sentinel-ui -d -p 8080:80 massenz/sentinel-nginx

echo "
The API server is now available at http://${DOCKER_IP}:9000
The UI can be reached at: http://${DOCKER_IP}:8080/web/
"
