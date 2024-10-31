package common.io

import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.regex.Pattern
import javax.inject.Inject

import play.api.libs.json.{Json, JsValue}

import common.rich.func.ToMoreFoldableOps._
import scalaz.std.option.optionInstance

import common.io.JsonableSaver.Encoding
import common.json.Jsonable
import common.json.ToJsonableOps._
import common.rich.RichT._
import common.rich.primitives.RichOption._
import common.rich.primitives.RichString._

/** Saves in json format to a file. */
class JsonableSaver @Inject() (@RootDirectory rootDirectory: DirectoryRef) {
  private val workingDir = rootDirectory.addSubDir("data").addSubDir("json")
  protected def jsonFileName[T: Manifest]: String =
    s"${manifest.runtimeClass.getSimpleName.removeAll(JsonableSaver.TrailingSlashes)}s.json"
  private def save[T: Manifest](js: JsValue): Unit =
    workingDir.addFile(jsonFileName).write(js.toString.getBytes(Encoding))

  /**
   * All files of the same type will be saved in the same file. Last save overwrites previous save.
   * Saves in the same order that was traversed, so load will return in the same order as well.
   */
  // TODO replace TraversableOnce with a Non-Empty list?
  def saveArray[T: Jsonable: Manifest](data: TraversableOnce[T]): Unit = {
    require(data.nonEmpty, s"Can't save empty data of type <$manifest>")
    save(data.toSeq.jsonify)
  }
  def saveObject[T: Jsonable: Manifest](obj: T): Unit = save(obj.jsonify)

  /**
   * Similar to save, but doesn't overwrite the data
   *
   * @param dataAppender
   *   A function that takes the old data and appends the new data. Since some constraints can exist
   *   on the data save, e.g., uniqueness, a simple concatenation of old and new data isn't enough.
   */
  // TODO this could perhaps be handled by a type class that would know its saving constraints?
  def update[T: Jsonable: Manifest](dataAppender: Seq[T] => TraversableOnce[T]): Unit =
    saveArray(dataAppender(loadArray))

  private def load[T: Manifest]: Option[JsValue] =
    workingDir.getFile(jsonFileName).map(_.bytes).map(new String(_, Encoding) |> Json.parse)
  /** Loads the previously saved entries, or returns an empty list. */
  def loadArray[T: Jsonable: Manifest]: Seq[T] = load.mapHeadOrElse(_.parse[Seq[T]], Nil)
  def loadObjectOpt[T: Jsonable: Manifest]: Option[T] = load.map(_.parse[T])
  /** Loads the previously saved entry, or throws an exception if no file has been found */
  def loadObject[T: Jsonable: Manifest]: T =
    load.getOrThrow(new FileNotFoundException(s"Couldn't find file for type <$manifest>")).parse[T]

  // Require T: Jsonable, otherwise T will always be inferred as Nothing
  def lastUpdateTime[T: Jsonable: Manifest]: Option[LocalDateTime] =
    workingDir.getFile(jsonFileName).map(_.lastModified)
}

private object JsonableSaver {
  private val TrailingSlashes = Pattern.compile("""\$""")
  private val Encoding = "UTF-8"
}
