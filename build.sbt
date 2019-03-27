name := "Mp3Streamer"
version := "1.0-SNAPSHOT"

resourceDirectory in Test := baseDirectory.value / "test-resources"
val scalaVersionStr = "2.12.7"
scalaVersion := scalaVersionStr
version := "1.0-SNAPSHOT"
routesGenerator := InjectedRoutesGenerator

val playWsStandaloneVersion = "1.1.2"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

// Add your own project settings her
resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Maven Repository" at "http://repo1.maven.org/maven2/",
  "Apache Snapshot Repository" at "http://repository.apache.org/snapshots/",
  Resolver.file("Local ivy repo", file(System.getProperty("user.home") + "/.ivy2/local"))(Resolver.ivyStylePatterns),
  Resolver.mavenLocal,
)
val akkaVersion = "2.5.4"
val monocleVersion = "1.5.0"
val playIterateesVersion = "2.6.1"
val scalazVersion = "7.2.15"
val guiceVersion = "4.2.0"

libraryDependencies ++= Seq(
  guice,
  "com.github.julien-truffaut" %% "monocle-core" % monocleVersion,
  "com.github.julien-truffaut" %% "monocle-macro" % monocleVersion,
  "com.github.mpilquist" %% "simulacrum" % "0.10.0",
  "com.google.inject" % "guice" % guiceVersion,
  "com.google.inject.extensions" % "guice-assistedinject" % guiceVersion,
  "com.h2database" % "h2" % "1.4.196",
  "com.jsuereth" %% "scala-arm" % "2.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.play" %% "play" % "2.6.6",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsStandaloneVersion,
  "com.typesafe.play" %% "play-iteratees" % playIterateesVersion,
  "com.typesafe.play" %% "play-iteratees-reactive-streams" % playIterateesVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playWsStandaloneVersion,
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "commons-validator" % "commons-validator" % "1.6",
  "io.reactivex" %% "rxscala" % "0.26.4",
  "me.tongfei" % "progressbar" % "0.7.1",
  "my.net.jthink" % "jaudiotagger" % "2.2.8-SNAPSHOT",
  "net.codingwell" %% "scala-guice" % "4.2.1",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.me" %% "scalacommon" % "1.0" changing(),
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  "org.scala-lang.modules" %% "scala-swing" % "2.0.0",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
  "org.scalamacros" % ("paradise_" + scalaVersionStr) % "2.1.0", // For some reason, it uses the full binary version
  "org.scalatest" %% "scalatest" % "3.0.4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
  "org.scalaz" %% "scalaz-concurrent" % scalazVersion,
  "org.scalaz" %% "scalaz-core" % scalazVersion,
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, LauncherJarPlugin)
    .settings(scalacOptions -= "-deprecation") // Fuck your deprecation bullshit.
