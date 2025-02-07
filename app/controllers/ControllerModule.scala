package controllers

import models.PosterLookup
import net.codingwell.scalaguice.ScalaModule

object ControllerModule extends ScalaModule {
  override def configure(): Unit =
    bind[PosterLookup].toInstance(PosterLookup.IOPosterLookup)
}
