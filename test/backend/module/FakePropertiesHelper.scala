package backend.module

import common.io.PropertiesHelper

private object FakePropertiesHelper extends PropertiesHelper {
  override def read(clazz: Class[_]): collection.Map[String, String] =
    Map.empty.withDefaultValue(Default)
  override def get(clazz: Class[_], key: String): Option[String] = Some(Default)
  override def getOrElse(clazz: Class[_], key: String, default: => String): String = Default

  private val Default = "deadbeaf"
}
