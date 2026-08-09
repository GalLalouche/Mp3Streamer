package mains.cover.image.serp

import com.google.inject.Provides
import common.io.PropertiesHelper
import net.codingwell.scalaguice.ScalaModule

private[cover] object SerpModule extends ScalaModule {
  @Provides @ApiKey
  private def apiKey(ph: PropertiesHelper): String = ph(getClass, "apiKey")
}
