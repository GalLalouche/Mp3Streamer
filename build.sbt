name := "Mp3Streamer"
version := "1.0-SNAPSHOT"

Compile / unmanagedSourceDirectories := Vector(baseDirectory.value / "app")
Compile / resourceDirectory := baseDirectory.value / "conf"
Test / unmanagedSourceDirectories := Vector(baseDirectory.value / "test")
Test / resourceDirectory := baseDirectory.value / "test-resources"
val scalaVersionStr = "2.13.17"
scalaVersion := scalaVersionStr
version := "1.0-SNAPSHOT"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.13.4").cross(CrossVersion.full))
Compile / doc / sources := Seq.empty
Compile / packageDoc / publishArtifact := false

val http4sVersion = "0.23.30"
resolvers ++= Seq(
  "Typesafe Ivy Repository".at("https://repo.typesafe.com/typesafe/ivy-releases/"),
  "Typesafe Maven Repository".at("https://repo.typesafe.com/typesafe/maven-releases/"),
  "Maven Repository".at("https://repo1.maven.org/maven2/"),
  "Apache Snapshot Repository".at("https://repository.apache.org/snapshots/"),
  Resolver.file("Local ivy repo", file(System.getProperty("user.home") + "/.ivy2/local"))(
    Resolver.ivyStylePatterns,
  ),
  Resolver.mavenLocal,
)
val catsVersion = "2.13.0"
val fs2Version = "3.12.2"
val guiceVersion = "7.0.0"
val monocleVersion = "2.1.0"
val playStandaloneVersion = "3.0.8"
val scalazVersion = "7.2.36"
val scribeVersion = "3.15.2"
libraryDependencies ++= Seq(
  // noinspection SbtDependencyVersionInspection (Newer JDK version).
  "ch.qos.logback" % "logback-core" % "1.3.15",
  "com.beachape" %% "enumeratum" % "1.7.5",
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "com.github.mpilquist" %% "simulacrum" % "0.19.0",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "com.google.inject" % "guice" % guiceVersion,
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion,
  "com.h2database" % "h2" % "1.4.196",
  "com.michaelpollmeier" %% "scala-arm" % "2.1",
  "com.outr" %% "scribe" % scribeVersion,
  "com.outr" %% "scribe-slf4j" % scribeVersion,
  "com.typesafe" % "config" % "1.4.3",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "commons-validator" % "commons-validator" % "1.9.0",
  "io.lemonlabs" %% "scala-uri" % "3.0.0",
  "io.reactivex" %% "rxscala" % "0.27.0",
  "me.tongfei" % "progressbar" % "0.10.1",
  "my.net.jthink" % "jaudiotagger" % "3.0.3-SNAPSHOT",
  "net.codingwell" %% "scala-guice" % "7.0.0",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.commons" % "commons-lang3" % "3.17.0",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.openjfx" % "javafx" % "21.0.6",
  "org.openjfx" % "javafx-controls" % "21.0.6",
  "org.playframework" %% "play-ahc-ws-standalone" % playStandaloneVersion,
  "org.playframework" %% "play-json" % "3.0.4",
  "org.playframework" %% "play-ws-standalone-json" % playStandaloneVersion,
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
  "org.scalafx" %% "scalafx" % "24.0.2-R36",
  "org.scalatest" %% "scalatest" % "3.0.9",
  "org.typelevel" %% "alleycats-core" % catsVersion,
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.xerial" % "sqlite-jdbc" % "3.49.0.0",
  ("org.me" %% "scalacommon" % "2.0").changing(),
  // Test stuff
  "com.softwaremill.sttp.client3" %% "core" % "3.10.3" % Test,
  "com.softwaremill.sttp.client3" %% "play2-json" % "3.10.3" % Test,
  "org.mockito" % "mockito-core" % "5.15.2" % Test,
  "org.scalacheck" %% "scalacheck" % "1.18.1" % Test,

  // http4s stuff
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
)

scalacOptions += "-Ymacro-annotations"
scalacOptions += "-deprecation"

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
lazy val runHttp4s = taskKey[Unit]("Test")
lazy val root = (project in file("."))
  .settings(
    reStart / mainClass := Some("http4s.Main"),
  )
