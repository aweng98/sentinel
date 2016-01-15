#!/bin/bash
# Copyright 2015 (c) AlertAvert.com. All rights reserved.
# Commercial use or modification of this software without a valid license is expressly forbidden

declare -r BASEDIR=${HOME}/.sentinel
declare -r TEST_CONF="${BASEDIR}/tests.conf"

if [[ ! -f ${TEST_CONF} ]]; then
    echo "ERROR - ${TEST_CONF} configuration file missing."
    exit 1
fi

sbt -Dconfig.file="${TEST_CONF}"  test

