==========================================
Sentinel - API-driven User Management SaaS
==========================================

.. image:: https://travis-ci.org/massenz/sentinel.svg?branch=develop  
  :target: https://travis-ci.org/massenz/sentinel                       

.. image:: https://coveralls.io/repos/massenz/sentinel/badge.png?branch=develop
  :target: https://coveralls.io/r/massenz/sentinel?branch=develop
  
:Author: Marco Massenzio (marco@alertavert.com)
:Version: 0.2
:Last Updated: 2014-09-28




Goals
-----

Implement a very simple API-driven service to enable application developers to manage user access
and permission management for their mobile and online web applications.

Architecture
------------

TBD

User Stories
------------

This project is managed via `Pivotal Tracker's Sentinel`_ project.

Implementation
--------------

Uses the Scala Play framework for the REST API, and core Scala for the backend implementation;
the persistence layer uses MongoDB (Casbah)

License
-------

We plan to eventually open source all the code under the Apache 2.0 License.

However, until development is complete and a release is ready, we need to keep the
copyright tight, so as to avoid unwanted forking.

The code is free to browse, copy and reuse in other projects as desired (attribution
and a link to this project would be appreciated, but not required).

Please include the following alongside your source code::

    // Code inspired by Sentinel Project:
    // See: http://github.com/massenz/sentinel

.. _Pivotal Tracker's Sentinel: https://www.pivotaltracker.com/n/projects/1082840
