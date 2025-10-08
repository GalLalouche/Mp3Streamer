package mains

import backend.module.StandaloneModule
import mains.cover.CoverModule
import mains.fixer.DetectLanguageTimeout
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.duration.{Duration, DurationInt}

import common.guice.RichModule.richModule

private object MainsModule extends ScalaModule {
  override def configure(): Unit = {
    install(CoverModule)
    install(StandaloneModule.overrideWith(new ScalaModule() {
      // Ensures the fixer process terminates after finishing, without waiting for the python
      // detector for too long.
      override def configure(): Unit =
        bind[Duration].annotatedWith[DetectLanguageTimeout].toInstance(5.seconds)
    }))
  }
}
