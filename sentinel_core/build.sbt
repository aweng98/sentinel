// Copyright (c) 2014 AlertAvert.com.  All rights reserved.

import scoverage.ScoverageSbtPlugin.ScoverageKeys._

name := "sentinel-core"

version := "0.3"

organization := "AlertAvert.com"

scalaVersion := "2.11.4"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

// set the initial commands when entering 'console' only
initialCommands in console := "import com.alertavert.sentinel._"

// To enable correct test execution for those tests needing
// a clean slate in the db
parallelExecution in Test := false

// Code coverage and support for coveralls.io
// See: https://github.com/scoverage/sbt-coveralls
coverageExcludedPackages := ".*\\.Permission;.*\\.errors"

coverageMinimum := 80

coverageFailOnMinimum := true

// Dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.mongodb" %% "casbah" % "2.7.3",
  "org.slf4j" % "slf4j-api" % "1.6.4"
)
