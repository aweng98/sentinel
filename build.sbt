name := "scala-sentinel"

version := "0.2"

scalaVersion := "2.10.1"

organization := "AlertAvert.com"

// append -deprecation to the options passed to the Scala compiler
scalacOptions ++= Seq("-deprecation", "-feature", "-language:postfixOps")

// set the initial commands when entering 'console' only
initialCommands in console := "import com.alertavert.sentinel._"

// AKKA Actors dependency
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
  "org.mongodb" %% "casbah" % "2.5.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.1",
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "org.slf4j" % "slf4j-simple" % "1.6.4"
)
