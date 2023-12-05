package common.io.google

import java.util.Properties

import net.codingwell.scalaguice.ScalaModule

object GoogleModule extends ScalaModule {
  override def configure(): Unit = {
    val properties = new Properties()
    properties.load(getClass.getResourceAsStream("tokens.properties"))
    bind[String].annotatedWith[ApiKey].toInstance(properties.get("apiKey").asInstanceOf[String])
    bind[String].annotatedWith[ApiID].toInstance(properties.get("apiID").asInstanceOf[String])
  }
}
