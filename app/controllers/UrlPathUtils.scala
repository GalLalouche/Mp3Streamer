package controllers

import java.io.File

import common.io.FileRef
import models.Song

trait UrlPathUtils extends UrlDecodeUtils {
  /**
  * A unique, URL-safe path of the song. Clients can use this url when requesting, e.g., lyrics or streaming
  * of songs from the server .
  */
  def encodePath(s: Song): String
  override def decode(s: String): String

  // While one could potentially use JsString(path).parseJsonable[Song] or something to that effect,
  // the path isn't really a JSON value, and also it tightly couples the code to the specific Jsonable
  // implementation.
  def parseSong(path: String): Song
  def parseFile(path: String): File
  def parseFileRef(path: String): FileRef
}
