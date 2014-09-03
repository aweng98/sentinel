// Copyright (c) 2014 AlertAvert.com.  All rights reserved.

name := "sentinel"

version := "0.1"

organization := "AlertAvert.com"

scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

// The REST project depends on Core Sentinel classes
lazy val sentinel_core = project

lazy val sentinel = (project in file("."))
    .enablePlugins(PlayScala)
    .aggregate(sentinel_core)
    .dependsOn(sentinel_core)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)
