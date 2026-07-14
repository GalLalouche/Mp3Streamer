package mains.cover.image

import java.util.Properties

private object ApiKeyHelper {
  def getApiKey(clazz: Class[_]): String = {
    val properties = new Properties()
    properties.load(clazz.getResourceAsStream("tokens.properties"))
    properties.get("apiKey").asInstanceOf[String]
  }
}
