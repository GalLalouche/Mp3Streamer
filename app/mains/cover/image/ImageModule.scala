package mains.cover.image

import java.util.Properties

import net.codingwell.scalaguice.ScalaModule

private[cover] object ImageModule extends ScalaModule {
  override def configure(): Unit = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("tokens.properties"))
    bind[String].annotatedWith[ApiKey].toInstance(properties.get("apiKey").asInstanceOf[String])
  }
}
