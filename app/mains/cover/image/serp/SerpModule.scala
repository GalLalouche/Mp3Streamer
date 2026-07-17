package mains.cover.image.serp

import com.google.inject.Provides
import mains.cover.image.ImageAPI
import net.codingwell.scalaguice.ScalaModule

import common.io.PropertiesHelper

private[cover] object SerpModule extends ScalaModule {
  override def configure(): Unit =
    bind[ImageAPI].to[API]

  @Provides @ApiKey
  private def apiKey(ph: PropertiesHelper): String = ph(getClass, "apiKey")
}
