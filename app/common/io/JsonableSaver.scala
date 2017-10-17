package common.io

import java.io.FileNotFoundException
import java.time.LocalDateTime

import common.rich.RichT._
import common.Jsonable
import common.rich.primitives.RichOption._
import play.api.libs.json.{Format, JsObject, JsValue, Json}

/** Saves in json format to a file. */
class JsonableSaver(implicit rootDir: DirectoryRef)
    extends Jsonable.ToJsonableOps {
  private val workingDir = rootDir addSubDir "data" addSubDir "json"
  protected def jsonFileName[T: Manifest]: String =
    s"${manifest.runtimeClass.getSimpleName.replaceAll("\\$", "")}s.json"
  private def save[T: Manifest](js: JsValue) {
    workingDir addFile jsonFileName write js.toString
  }

  /**
   * All files of the same type will be saved in the same file. Last save overwrites
   * previous save. Saves in the same order that was traversed, so load will return in the same order as well.
   */
  // TODO replace TraversableOnce with a Non-Empty list?
  def save[T: Format : Manifest](data: TraversableOnce[T]) {
    require(data.nonEmpty, s"Can't save empty data of type <$manifest>")
    save(data.toSeq.jsonify)
  }
  def save[T: Format : Manifest](obj: T): Unit = save(obj.jsonify)

  /**
   * Similar to save, but doesn't overwrite the data
   * @param dataAppender A function that takes the old data and appends the new data. Since some constraints can exist
   *                     on the data save, e.g., uniqueness, a simple concatenation of old and new data isn't enough.
   */
  //TODO this could perhaps be handled by a type class that would know its saving constraints?
  def update[T: Format : Manifest](dataAppender: Seq[T] => TraversableOnce[T]) {
    save(dataAppender(loadArray))
  }

  private def load[T: Manifest]: Option[JsValue] =
    workingDir getFile jsonFileName map (_.readAll |> Json.parse)
  /** Loads the previously saved entries, or returns an empty list. */
  def loadArray[T: Format : Manifest]: Seq[T] = {
    load.map(_.parse[Seq[T]]) getOrElse Nil
  }
  /** Loads the previously saved entry, or throws an exception if no file has been found */
  def loadObject[T: Format : Manifest]: T = {
    val js = load getOrThrow new FileNotFoundException(s"Couldn't find file for type <$manifest>")
    js.asInstanceOf[JsObject].parse
  }

  // Require T: Format, otherwise T will always be inferred as Nothing
  def lastUpdateTime[T: Format : Manifest]: Option[LocalDateTime] = workingDir getFile jsonFileName map (_.lastModified)
}
