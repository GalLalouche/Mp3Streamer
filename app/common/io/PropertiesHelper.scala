package common.io

// TODO SoftwareDesign could be an interesting question: The code needs to behave differently in
//  production and test environments. Originally this failed in tests and was a singleton.
//  How would you use guice to allow for different behaviors based on the environment.
trait PropertiesHelper {
  /**
   * Throws [[java.io.FileNotFoundException]] if a file named `tokens.properties` is not found in
   * the same package as the class.
   */
  def read(clazz: Class[_]): collection.Map[String, String]

  def apply(clazz: Class[_], key: String): String = getOrElse(
    clazz,
    key,
    throw new NoSuchElementException(s"Missing property <$key> in ${fileName(clazz)}"),
  )

  def get(clazz: Class[_], key: String): Option[String] = read(clazz).get(key)
  def getOrElse(clazz: Class[_], key: String, default: => String): String =
    read(clazz).getOrElse(key, default)

  protected final def fileName(clazz: Class[_]): String = clazz.getResource(FileName).getFile
  protected val FileName = "tokens.properties"
}
