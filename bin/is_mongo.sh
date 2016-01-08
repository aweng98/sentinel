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

    echo "Usage: is_mongo [-q] server[:port]

    -q      (optional) if present, no output is emitted to stdout.
    server  (required) the address (or hostname) of the server.
    port    (optional; default: 27017) the port the server is listening on.
    "
}

declare -a QUIET

if [[ $1 == "-q" ]]; then
    QUIET=1
    shift 1
fi

if [[ -z $1 ]]; then
    usage
    exit 1
fi
declare -r SERVER=$1

FAIL=$(mongo ${SERVER}/test --eval "printjson(db.serverStatus())" 2>&1 | grep "connect failed" | cut -f 3 -d ' ')

if [[ $FAIL == "failed" ]]; then
    if [[ -z ${QUIET} ]]; then echo "Server ${SERVER} is not active"; fi
    exit 1
fi

if [[ -z ${QUIET} ]]; then echo "Server ${SERVER} is active"; fi
