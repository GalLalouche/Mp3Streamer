package controllers

import java.io.File
import java.net.URLDecoder

import models.Song

private object Utils {
  def parseSong(path: String): Song = Song(parseFile(path))
  def parseFile(path: String): File = new File(URLDecoder.decode(path, "UTF-8"))
}
