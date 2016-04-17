enablePlugins(ScalaJSPlugin)

name := "Scala.js Mp3"

scalaVersion := "2.11.8" // or any other Scala version >= 2.10.2

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"
libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
skip in packageJSDependencies := false
jsDependencies +=
    "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js"
