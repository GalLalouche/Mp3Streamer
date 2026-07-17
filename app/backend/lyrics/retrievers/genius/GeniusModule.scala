package backend.lyrics.retrievers.genius

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule

import common.io.PropertiesHelper

private[lyrics] object GeniusModule extends ScalaModule {
  @Provides @Singleton @AccessToken
  private def accessToken(ph: PropertiesHelper): String = ph(getClass, "accessToken")
}
