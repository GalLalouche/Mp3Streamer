package mains.cover.image.scrappa

import com.google.inject.Provides
import common.io.PropertiesHelper
import net.codingwell.scalaguice.ScalaModule

private[cover] object ScrappaModule extends ScalaModule {
  @Provides @ApiKey
  private def apiKey(ph: PropertiesHelper): String = ph(getClass, "apiKey")
}
