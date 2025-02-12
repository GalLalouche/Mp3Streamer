// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/ivy-releases/")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
