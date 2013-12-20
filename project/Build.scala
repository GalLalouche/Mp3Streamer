import sbt._
import Keys._
import play.Project._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._
object ApplicationBuild extends Build {

	val appName = "Mp3Streamer"
	val appVersion = "1.0-SNAPSHOT"

	val appDependencies = Seq(
		// Add your project dependencies here,
		jdbc,
		anorm,
		"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
		"org.specs2" %% "specs2" % "2.1.1" % "test",
		"org.scala-lang" % "scala-actors" % "2.10.0",
		"org.mockito" % "mockito-all" % "1.9.5" % "test",
		"com.typesafe.akka" %% "akka-actor" % "[2.0,)",
		"com.typesafe.akka" %% "akka-testkit" % "[2.0,)" % "test",
		"org.apache.commons" % "commons-io" % "[1.3.2, )",
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(
		// Add your own project settings here 
		//unmanagedBase <<= baseDirectory { base => base / "test" }
		EclipseKeys.withSource := true,
		resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
		resolvers += "Maven Repository" at "http://repo1.maven.org/maven2/",
		resolvers += "Apache Snapshot Repository" at "http://repository.apache.org/snapshots/"
	)

}
