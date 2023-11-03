package backend.lyrics.retrievers.genius

import java.util.Properties

import net.codingwell.scalaguice.ScalaModule

private[lyrics] object GeniusModule extends ScalaModule {
  override def configure(): Unit = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("tokens.properties"))
    bind[String]
      .annotatedWith[AccessToken]
      .toInstance(properties.get("accessToken").asInstanceOf[String])
  }
}
