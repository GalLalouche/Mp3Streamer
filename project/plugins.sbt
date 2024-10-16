// Comment to get more information during initialization
logLevel := Level.Warn

resolvers += "Typesafe repository".at("https://repo.typesafe.com/typesafe/ivy-releases/")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.22")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
