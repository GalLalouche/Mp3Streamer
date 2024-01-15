package controllers

import java.io.File

import models.Song

import common.io.{FileRef, PathRef}

trait UrlPathUtils extends UrlDecodeUtils {
  /**
   * Returns a unique, URL-safe path of the song. Clients can use this url when requesting, e.g.,
   * lyrics or streaming of songs from the server.
   */
  def encodePath(s: Song): String = encodePath(s.file)
  def encodePath(f: PathRef): String
  override def decode(s: String): String

  // These shouldn't be used by formatters. Instead, the controllers should use UrlPathUtils to
  // decode the path before sending it to the formatter, since it's the controller's job to handle
  // decoding.
  @deprecated def parseSong(path: String): Song
  @deprecated def parseFile(path: String): File
  @deprecated def parseFileRef(path: String): FileRef
}
