package common.io

import java.io.FileNotFoundException
import java.util.Properties

import scala.jdk.CollectionConverters._

object ProductionPropertiesHelper extends PropertiesHelper {
  override def read(clazz: Class[_]): collection.Map[String, String] = {
    val properties = new Properties()
    val stream = clazz.getResourceAsStream(FileName)
    if (stream == null)
      throw new FileNotFoundException(s"Missing ${fileName(clazz)}")
    properties.load(stream)
    properties.asScala
  }
}
