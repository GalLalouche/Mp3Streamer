package mains.fixer

import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.duration.{Duration, DurationInt}

object FixerModule extends ScalaModule {
  override def configure(): Unit =
    bind[Duration].annotatedWith[DetectLanguageTimeout].toInstance(1 minute)
}
