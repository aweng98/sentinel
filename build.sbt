// Copyright (c) 2014 AlertAvert.com.  All rights reserved.

import CoverallsPlugin.CoverallsKeys._
import scoverage.ScoverageSbtPlugin.ScoverageKeys._

scalaVersion := "2.11.1"

name := "sentinel"

version := "0.2-SNAPSHOT"

organization := "AlertAvert.com"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

// Code coverage and support for coveralls.io
// See: https://github.com/scoverage/sbt-coveralls
coverageExcludedPackages := 
        "<empty>;controllers\\..*Reverse.*"

//coverallsFailBuildOnError := true

//org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

//encoding := "ISO-8859-1"



// The REST project depends on Core Sentinel classes
lazy val sentinel_core = project

lazy val sentinel = (project in file("."))
    .enablePlugins(PlayScala)
    .aggregate(sentinel_core)
    .dependsOn(sentinel_core)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  cache
)
