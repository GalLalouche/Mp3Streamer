name := "Mp3Streamer"
version := "1.0-SNAPSHOT"

Compile / unmanagedSourceDirectories := Vector(baseDirectory.value / "app")
Compile / resourceDirectory := baseDirectory.value / "conf"
Test / unmanagedSourceDirectories := Vector(baseDirectory.value / "test")
Test / resourceDirectory := baseDirectory.value / "test-resources"
val scalaVersionStr = "2.12.15"
scalaVersion := scalaVersionStr
version := "1.0-SNAPSHOT"

addCompilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full))
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin(("org.typelevel" %% "kind-projector" % "0.13.3").cross(CrossVersion.full))
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
val akkaVersion = "2.6.1"
val monocleVersion = "1.5.0"
val scalazVersion = "7.2.15"
val guiceVersion = "4.2.0"
val playStandaloneVersion = "2.1.11"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % "1.3.15",
  "com.beachape" %% "enumeratum" % "1.5.13",
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "com.github.mpilquist" %% "simulacrum" % "0.10.0",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "com.google.inject" % "guice" % guiceVersion,
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion,
  "com.h2database" % "h2" % "1.4.196",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "com.outr" %% "scribe" % "3.15.0",
  "com.typesafe" % "config" % "1.4.3",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playStandaloneVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playStandaloneVersion,
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "commons-validator" % "commons-validator" % "1.6",
  "io.lemonlabs" %% "scala-uri" % "3.0.0",
  "io.reactivex" %% "rxscala" % "0.26.4",
  "me.tongfei" % "progressbar" % "0.9.5",
  "my.net.jthink" % "jaudiotagger" % "2.2.9-SNAPSHOT",
  "net.codingwell" %% "scala-guice" % "4.2.1",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.apache.commons" % "commons-lang3" % "3.17.0",
  "org.jsoup" % "jsoup" % "1.12.1",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.playframework" %% "play-json" % "3.0.4",
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.scalafx" %% "scalafx" % "17.0.1-R26",
  "org.scalamacros" % ("paradise_" + scalaVersionStr) % "2.1.1", // For some reason, it uses the full binary version
  "org.scalatest" %% "scalatest" % "3.0.4",
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.xerial" % "sqlite-jdbc" % "3.32.3.2",
  ("org.me" %% "scalacommon" % "1.0").changing(),

  // http4s stuff
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
)

scalacOptions += "-Ypartial-unification"
lazy val runHttp4s = taskKey[Unit]("Test")
lazy val root = (project in file("."))
  .settings(
    scalacOptions -= "-deprecation",
    reStart / mainClass := Some("http4s.Main"),
  ) // Fuck your deprecation bullshit.
