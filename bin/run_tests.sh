#!/bin/bash
# Copyright 2015 (c) AlertAvert.com. All rights reserved.
# Commercial use or modification of this software without a valid license is expressly forbidden

# Detects whether a MongoDB server is listening on the given address (and, optionally, port).
# The default port is 27017.
#
# Use the exit code to determine outcome: 0, there is a listening `mongod` on the given address;
#     1, there is not.
#

function usage {

    echo "Usage: run_tests [server]

    server  (optional) the address (or hostname) of the server; if omitted, SENTINEL_MONGO
            from the system environment is used instead.
    "
}

declare -a SERVER=$1
declare -r DB_NAME="sentinel-test"
declare -r BINDIR=$(dirname $0)

if [[ -z $1 ]]; then
    if [[ -z ${SENTINEL_MONGO} ]]; then
        echo "ERROR: Either \$SENTINEL_MONGO must be defined, or you must pass the "\
             "server address as the first argument"
        usage
        exit 1
    fi
    SERVER=${SENTINEL_MONGO}
fi


${BINDIR}/is_mongo.sh -q ${SERVER}/${DB_NAME}
if [[ $? != 0 ]]; then
    echo "ERROR: Could not find an active MongoDB server at ${SERVER}"
    exit 1
fi

sbt -Dsentinel.test.db_uri="mongodb://${SERVER}/${DB_NAME}" test
