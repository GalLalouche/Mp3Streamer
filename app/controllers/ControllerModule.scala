package controllers

import http4s.Http4sUtils
import models.PosterLookup
import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
    bind[Encoder].toInstance(Http4sUtils.encode)
    bind[Decoder].toInstance(Http4sUtils.decode)
  }
}
