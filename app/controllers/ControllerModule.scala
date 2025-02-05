package controllers

import com.google.inject.Provides
import models.PosterLookup
import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UrlPathUtils].toInstance(PlayUrlPathUtils)
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
  }

  @Provides private def provideUrlDecoder(u: UrlPathUtils): UrlDecodeUtils = u
}
