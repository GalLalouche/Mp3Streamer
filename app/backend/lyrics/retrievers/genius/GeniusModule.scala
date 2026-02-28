package backend.lyrics.retrievers.genius

import java.util.Properties

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule

private[lyrics] object GeniusModule extends ScalaModule {
  @Provides @Singleton @AccessToken
  private def accessToken: String = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("tokens.properties"))
    properties.get("accessToken").asInstanceOf[String]
  }
}
