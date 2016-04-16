import play.routes.compiler.StaticRoutesGenerator
import play.sbt.PlayScala
import sbt.Keys._
import sbt._

object ApplicationBuild extends Build {

  val appName = "Mp3Streamer"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.typesafe.akka" %% "akka-actor" % "[2.0,)",
    "com.typesafe.akka" %% "akka-testkit" % "[2.0,)" % "test",
    "com.typesafe.play" % "play_2.11" % "2.5.2",
    "io.reactivex" %% "rxscala" % "[0.25, )",
    "org.apache.commons" % "commons-io" % "[1.3.2, )",
    "org.jsoup" % "jsoup" % "1.8.3",
    "org.me" % "scalacommon_2.10" % "1.0",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scala-lang" % "scala-actors" % "2.11.0",
    "org.scala-lang" % "scala-swing" % "[2.11, )",
    "org.scala-lang" % "scala-swing" % "[2.11, )",
    "org.scalatest" % "scalatest_2.11" % "2.2.1",
    "org.scalaz" % "scalaz-core_2.11" % "7.2.0"
  )

  val main = Project(appName, file(".")).enablePlugins(PlayScala).settings(
    scalaVersion := "2.11.1",
    version := appVersion,
    libraryDependencies ++= appDependencies,
    // Add your own project settings here
    //unmanagedBase <<= baseDirectory { base => base / "test" }
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Maven Repository" at "http://repo1.maven.org/maven2/",
    resolvers += "Apache Snapshot Repository" at "http://repository.apache.org/snapshots/",
    resolvers += Resolver.file("Local repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns),
    javaOptions ++= Seq("-Xmx4000M", "-Xms2024M", "-XX:MaxPermSize=2000M")
  )
}
