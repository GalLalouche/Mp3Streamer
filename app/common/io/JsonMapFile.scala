package common.io

import java.io.InputStream

import scala.io.Source

import common.json.ToJsonableOps.jsonifyString

/** Expects a file where each line is a JsonArray of two strings, e.g., ["a", "b"]. */
object JsonMapFile {
  def readJsonMap(stream: InputStream): Map[String, String] =
    Source
      .fromInputStream(stream, "utf-8")
      .getLines()
      .map(_.parseJsonable[Seq[String]].toVector.ensuring(_.size == 2))
      .map { case Vector(key, value) => (key, value) }
      .toMap
}
