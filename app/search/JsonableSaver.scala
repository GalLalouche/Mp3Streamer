package search
import common.io.DirectoryRef
import play.api.libs.json.{JsObject, Json}

/**
 * Saves in json format to a file.
 * @param workingDir The dir to save and load files from
 */
private class JsonableSaver(implicit rootDir: DirectoryRef) {
  private val workingDir = rootDir addSubDir "data" addSubDir "json"
  private def jsonFileName[T : Manifest]: String =
    s"${implicitly[Manifest[T]].runtimeClass.getSimpleName.replaceAll("\\$", "") }s.json"
  /**
   * All files of the same type will be saved in the same file. Last save overwrites
   * previous save. Saves in the same order that was traversed, so load will return in the same order as well.
   */
  def save[T: Jsonable : Manifest](data: TraversableOnce[T]) {
    val m = implicitly[Manifest[T]]
    require(data.nonEmpty, s"Can't save empty data of type <$m>")
    val file = workingDir addFile jsonFileName(m)
    file.write(data.map(implicitly[Jsonable[T]].jsonify).mkString("\n"))
  }
  /**
   * Similar to save, but doesn't overwrite the data
   * @param dataAppender A function that takes the old data and appends the new data. Since some constraints can exist
   * on the data save, e.g., uniqueness, a simple concatenation of old and new data isn't enough.
   */
  //TODO this could perhaps be handled by a typeclass that would know its saving constraints?
  def update[T: Jsonable : Manifest](dataAppender: Seq[T] => TraversableOnce[T]) {
    save(dataAppender(load))
  }
  /** Loads the previously saved entry, or returns an empty list. */
  def load[T: Jsonable : Manifest]: Seq[T] =
    workingDir.getFile(jsonFileName[T])
      .map(_.lines
        .map(Json.parse)
        .map(_.as[JsObject])
        .map(implicitly[Jsonable[T]].parse))
      .getOrElse(Nil)
}
