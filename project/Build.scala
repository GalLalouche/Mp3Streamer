import sbt._
import Keys._
import play.Project._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
object ApplicationBuild extends Build {

  val appName         = "Mp3Streamer"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
    "org.specs2" %% "specs2" % "2.1.1" % "test",
    "org.scala-lang" % "scala-actors" % "2.10.0"
  )
  
  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here 
	//unmanagedBase <<= baseDirectory { base => base / "test" }
	sbt.Keys.fork := false,
	sbt.Keys.fork in Compile := false,
	sbt.Keys.fork in Test := false,
	EclipseKeys.withSource := true
  )

}
