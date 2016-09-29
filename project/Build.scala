import play.routes.compiler.StaticRoutesGenerator
import play.sbt.PlayScala
import play.sbt.routes.RoutesKeys._
import sbt.Keys._
import sbt._

object ApplicationBuild extends Build {
  val main = Project("Mp3Streamer", file(".")).enablePlugins(PlayScala).settings(
    scalaVersion := "2.11.8",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      // Add your project dependencies here,
      "com.h2database" % "h2" % "1.4.192",
      "com.typesafe.akka" %% "akka-actor" % "2.4.8",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.8" % "test",
      "com.typesafe.play" %% "play" % "2.5.4",
      "com.typesafe.play" %% "play-ws" % "2.5.4",
      "com.typesafe.slick" %% "slick" % "3.0.0",
      "io.reactivex" %% "rxscala" % "0.26.2",
      "my.net.jthink" % "jaudiotagger" % "2.2.6-SNAPSHOT" changing(),
      "org.apache.commons" % "commons-io" % "1.3.2",
      "org.jsoup" % "jsoup" % "1.8.3",
      "org.me" %% "scalacommon" % "1.0" changing(),
      "org.mockito" % "mockito-all" % "1.9.5",
      "org.scala-js" %% "scalajs-library" % "0.6.8",
      "org.scala-lang" % "scala-actors" % "2.11.0",
      "org.scala-lang" % "scala-swing" % "2.11.0-M7",
      "org.scalacheck" %% "scalacheck" % "1.12.1" % "test",
      "org.scalatest" %% "scalatest" % "2.2.6",
      "org.scalaz" %% "scalaz-core" % "7.2.0",
      "org.xerial" % "sqlite-jdbc" % "3.7.2"
    ),
    // Add your own project settings here
    //unmanagedBase <<= baseDirectory { base => base / "test" }
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Maven Repository" at "http://repo1.maven.org/maven2/",
    resolvers += "Apache Snapshot Repository" at "http://repository.apache.org/snapshots/",
    resolvers += Resolver.file("Local ivy repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.mavenLocal,
    javaOptions ++= Seq("-Xmx4000M", "-Xms2024M", "-XX:MaxPermSize=2000M"),
    routesGenerator := StaticRoutesGenerator
  )
}
