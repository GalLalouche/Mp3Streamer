package common.io

import java.io.FileNotFoundException
import java.time.LocalDateTime

import common.Jsonable
import common.rich.RichT._
import common.rich.primitives.RichOption._
import play.api.libs.json._

/** Saves in json format to a file. */
class JsonableSaver(implicit rootDir: DirectoryRef) {
  private val workingDir = rootDir addSubDir "data" addSubDir "json"
  protected def jsonFileName[T: Manifest]: String =
    s"${implicitly[Manifest[T]].runtimeClass.getSimpleName.replaceAll("\\$", "") }s.json"
  private def save[T: Manifest](js: JsValue) {
    workingDir addFile jsonFileName write js.toString
  }

  /**
   * All files of the same type will be saved in the same file. Last save overwrites
   * previous save. Saves in the same order that was traversed, so load will return in the same order as well.
   */
  def save[T: Jsonable : Manifest](data: TraversableOnce[T]) {
    val m = implicitly[Manifest[T]]
    require(data.nonEmpty, s"Can't save empty data of type <$m>")
    save(data.toSeq |> implicitly[Jsonable[T]].jsonify)
  }
  def save[T: Jsonable : Manifest](obj: T) {
    save(implicitly[Jsonable[T]].jsonify(obj))
  }

  /**
   * Similar to save, but doesn't overwrite the data
   * @param dataAppender A function that takes the old data and appends the new data. Since some constraints can exist
   *                     on the data save, e.g., uniqueness, a simple concatenation of old and new data isn't enough.
   */
  //TODO this could perhaps be handled by a typeclass that would know its saving constraints?
  def update[T: Jsonable : Manifest](dataAppender: Seq[T] => TraversableOnce[T]) {
    save(dataAppender(loadArray))
  }

  private def load[T: Manifest]: Option[JsValue] =
    workingDir getFile jsonFileName map (_.readAll |> Json.parse)
  /** Loads the previously saved entries, or returns an empty list. */
  def loadArray[T: Jsonable : Manifest]: Seq[T] =
    load.map(_.as[JsArray] |> implicitly[Jsonable[T]].parse)
        .getOrElse(Nil)
  /** Loads the previously saved entry, or throws an exception if no file has been found */
  def loadObject[T: Jsonable : Manifest]: T = {
    val js = load getOrThrow new FileNotFoundException(s"Couldn't find file for type <${implicitly[Manifest[T]] }>")
    js.asInstanceOf[JsObject] |> implicitly[Jsonable[T]].parse
  }

  // Require T: Jsonable, otherwise T will always be inferred as Nothing
  def lastUpdateTime[T: Jsonable : Manifest]: Option[LocalDateTime] = workingDir getFile jsonFileName map (_.lastModified)
}
