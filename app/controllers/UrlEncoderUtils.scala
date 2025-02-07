package controllers

import models.Song

import common.io.PathRef

trait UrlEncoderUtils {
  /**
   * Returns a unique, URL-safe path of the song. Clients can use this url when requesting, e.g.,
   * lyrics or streaming of songs from the server.
   */
  def apply(s: Song): String = apply(s.file)
  def apply(f: PathRef): String
}
