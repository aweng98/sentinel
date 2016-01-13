#!/usr/bin/env python
#
# Copyright 2015 (c) AlertAvert.com. All rights reserved.
# Commercial use or modification of this software without a valid license is expressly forbidden

"""
Builds the API Server as a Docker container.

It needs a Dockerfile, named by default ``build/sentinel.Dockerfile`` (change this with
--dockerfile).

Unless specified via the ``--work-dir`` flag, it assumes that the current directory is the
root Sentinel folder.
"""

__author__ = 'Marco Massenzio'
__email__ = 'marco@alertavert.com'

import argparse
import logging
import os

# The `sh` module is super-useful, but not part of the standard python library.
try:
    from sh import cp, docker, mv, sbt, ErrorReturnCode
except ImportError as imperr:
    print("Is the `sh` module installed? If not, please install with `pip install sh` (use of "
          "virtualenv is strongly recommended). [{}]".format(imperr))
    exit(1)

from fix_bin import fix_binfile


LOG_FORMAT = '%(asctime)s [%(levelname)-5s] %(message)s'
DOCKER_IMAGE = 'massenz/sentinel-apiserver'
BINFILE = 'target/docker/files/opt/docker/bin/sentinel'


def parse_args():
    """ Parse command line arguments and returns a configuration object.

    @return: the configuration object, arguments accessed via dotted notation
    @rtype: dict
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--dockerfile', default='build/sentinel.Dockerfile',
                        help="The location of the Dockerfile; if not an absolute path, it will"
                             "be taken to be relative to --work-dir.")
    parser.add_argument('--logdir', default=None,
                        help="The direcory to use for the log files; if not given, uses stdout.")
    parser.add_argument('--debug', default=False, action='store_true')
    parser.add_argument('--work-dir', default=os.getcwd(),
                        help="The root directory for the project; while building the container "
                             "it needs a set of very specific sub-folders, rooted in this one.")
    return parser.parse_args()


def main(cfg):
    workdir = cfg.work_dir
    if not os.path.exists(workdir):
        logging.error("Folder {} does not exist; --work-dir must point to the root of the "
                      "Sentinel project")
        exit(1)

    logging.info("Building Sentinel API Server Docker container (project root dir: {})".format(
        workdir))
    dockerfile = os.path.join(workdir, cfg.dockerfile)

    logging.info("Building the Server JARs (via SBT)")
    try:
        os.chdir(workdir)
        for line in sbt('docker:stage', _iter=True):
            logging.info(line[:-1])
        logging.info("Fixing shell script CLASSPATH")
        fix_binfile(os.path.join(workdir, BINFILE))
    except ErrorReturnCode:
        logging.error("Build failure - please see log output and fix the build")
        exit(1)

    logging.info("Creating Dockerfile from template {}".format(dockerfile))
    if not os.path.exists(dockerfile):
        logging.error("Template {} does not exist, aborting.".format(dockerfile))
        exit(1)
    cp(dockerfile, os.path.join(workdir, 'target', 'docker', 'Dockerfile'))

    logging.info("Building docker image...")
    try:
        # TODO(marco): add versioning info to image name.
        # Depends on story 111581264.
        for line in docker('build', '-t', DOCKER_IMAGE, 'target/docker', _iter=True):
            logging.info(line[:-1])
    except ErrorReturnCode as ex:
        logging.error(ex)
        logging.error("Failed to generate the Docker image")
        exit(1)
    # TODO: parse stdout to extract the actual image name
    logging.info("Successfully built Docker image `{}` for API Server".format(
        DOCKER_IMAGE))
    logging.info("You can now start the container using\n"
                 "`docker run --link mongo-dev -p 9000:9000 --name sentinel-api -d {}`".format(
        DOCKER_IMAGE))
    logging.info("Docker image build complete.")


if __name__ == '__main__':
    config = parse_args()
    logfile = None
    if config.logdir:
        logfile = os.path.join(os.path.expanduser(config.logdir), 'build-api-server.log')
    level = logging.DEBUG if config.debug else logging.INFO
    if logfile:
        print("All logging going to {} (debug info {})".format(
            logfile, 'enabled' if config.debug else 'disabled'))

    logging.basicConfig(filename=logfile, level=level, format=LOG_FORMAT,
                        datefmt="%Y-%m-%d %H:%M:%S")
    main(config)
