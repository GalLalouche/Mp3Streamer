package controllers

import models.PosterLookup
import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UrlEncoderUtils].toInstance(PlayUrlEncoder)
    bind[UrlDecodeUtils].toInstance(PlayUrlDecoder)
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }
}
