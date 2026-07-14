package mains.cover.image.scrappa

import mains.cover.image.{ApiKeyHelper, ImageAPI}
import net.codingwell.scalaguice.ScalaModule

private[cover] object ScrappaModule extends ScalaModule {
  override def configure(): Unit = {
    bind[String].annotatedWith[ApiKey].toInstance(ApiKeyHelper.getApiKey(getClass))
    bind[ImageAPI].to[API]
  }
}
