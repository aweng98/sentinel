resolvers ++= Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    Classpaths.sbtPluginReleases
)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")

// Adding Test Coverage (scoverage) and Coveralls.io support
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.0.0")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "0.99.0")
