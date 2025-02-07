package controllers

import models.Song

import common.io.PathRef

trait UrlPathUtils extends UrlDecodeUtils {
  /**
   * Returns a unique, URL-safe path of the song. Clients can use this url when requesting, e.g.,
   * lyrics or streaming of songs from the server.
   */
  def encodePath(s: Song): String = encodePath(s.file)
  def encodePath(f: PathRef): String
  override def decode(s: String): String
}
