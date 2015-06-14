# Created MM, 2015-06-07
#
# Builds a docker container running Play framework
# and the Sentinel application layer

# To use this Dockerfile, you must first run ``sbt docker:stage``
# then copy this file to target/docker/Dockerfile, overwriting the one
# created by sbt.
#
# Build the docker image: ``docker build -t massenz/sentinel .``
# then run the container: ``docker run -p 80:9000 -d massenz/sentinel``

# TODO(marco): automate the process


FROM java:8
MAINTAINER Marco Massenzio <marco@alertavert.com>
ADD files /
COPY files/opt/docker/conf/bootstrap.json /etc/sentinel/bootstrap.json
WORKDIR /opt/docker
RUN ["chown", "-R", "daemon", "."]
USER daemon
ENTRYPOINT ["bin/sentinel"]
CMD []
EXPOSE 9000
