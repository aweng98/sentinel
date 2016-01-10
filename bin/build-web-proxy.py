#!/usr/bin/env python
#
# Copyright 2015 (c) AlertAvert.com. All rights reserved.
# Commercial use or modification of this software without a valid license is expressly forbidden

"""
Builds the WebUi part of Sentinel in a ngnix-proxy Docker container

It will require the following:

- a Dockerfile, named by default ``build/web-ui.Dockerfile`` (change this with
  --dockerfile)

- a folder containing the Web UI static files, by default ``public`` (change this
  with --public)

- the NGINX configuration file (by default ``build/nginx.conf, change with --config)

Run this script from ``Sentinel`` project root directory, and it should "just work."
"""

__author__ = 'Marco Massenzio'
__email__ = 'marco@alertavert.com'

import argparse
import logging
import os
from sh import tar, cp, docker, ErrorReturnCode
import tempfile


LOG_FORMAT = '%(asctime)s [%(levelname)-5s] %(message)s'
DOCKER_IMAGE = 'massenz/sentinel-nginx'


def parse_args():
    """ Parse command line arguments and returns a configuration object.

    @return: the configuration object, arguments accessed via dotted notation
    @rtype: dict
    """
    parser = argparse.ArgumentParser()
    parser.add_argument('--dockerfile', default='build/web-ui.Dockerfile',
                        help="the location of the Dockerfile Jinja template")
    parser.add_argument('--logdir', default=None,
                        help="The direcory to use for the log files; if not given, uses stdout")
    parser.add_argument('--public', '-p', default='public',
                        help='The location of the static files (HTML, CSS, JS, etc.)')
    parser.add_argument('--config', '-c', default='build/nginx.conf',
                        help='The NGINX configuration file')
    parser.add_argument('--debug', default=False, action='store_true')
    return parser.parse_args()


def main(cfg):
    logging.info("Building Sentinel Web UI Docker proxy")
    dockerfile = cfg.dockerfile
    pub_dir = cfg.public
    nginx_conf = cfg.config

    logging.info("Copying all static file to Nginx container from `{}` folder".format(pub_dir))
    if not os.path.exists(pub_dir):
        logging.error("Static files directory {} does not exist, aborting.".format(
            os.path.abspath(pub_dir)))
        exit(1)

    logging.info("Creating Dockerfile from template {}".format(dockerfile))
    if not os.path.exists(dockerfile):
        logging.error("Template {} does not exist, aborting.".format(dockerfile))
        exit(1)

    workdir = tempfile.mkdtemp()
    logging.info("Created temporary directory {}".format(workdir))
    tarfile = os.path.join(workdir, 'sentinel-webui.tar.gz')
    tar("caf", tarfile, '-C', pub_dir, '--exclude-tag-all=NO_EXPORT', '.')
    logging.info("Compressed all public static files to {}".format(tarfile))

    # TODO(marco): fix the hard-coded file names
    nginx_files = [nginx_conf, 'build/nginx.crt', 'build/nginx.key']
    for nf in nginx_files:
        if os.path.exists(nf):
            cp(nf, workdir)
            logging.info("Copied ngnix configuration file {} to {}".format(
                nf, os.path.join(workdir, nginx_conf)
            ))
    logging.info("Building docker image...")
    cp(dockerfile, os.path.join(workdir, 'Dockerfile'))
    try:
        dout = docker('build', '-t', DOCKER_IMAGE, workdir)
        logging.info(dout)
    except ErrorReturnCode as ex:
        logging.error(ex)
        logging.error("Failed to generate the Docker image")
        exit(1)
    # TODO: parse stdout to extract the actual image name
    logging.info("Successfully built Docker image `{}` for Sentinel Web UI".format(
        DOCKER_IMAGE))
    logging.info("You can now start the container using "
                 "`docker run --name sentinel-web -d -p 8080:80 sentinel-nginx`")
    logging.info("Docker image build complete.")


if __name__ == '__main__':
    config = parse_args()
    logfile = None
    if config.logdir:
        logfile = os.path.join(os.path.expanduser(config.logdir), 'build-web-proxy.log')
    level = logging.DEBUG if config.debug else logging.INFO
    if logfile:
        print("All logging going to {} (debug info {})".format(
            logfile, 'enabled' if config.debug else 'disabled'))

    logging.basicConfig(filename=logfile, level=level, format=LOG_FORMAT,
                        datefmt="%Y-%m-%d %H:%M:%S")
    main(config)
