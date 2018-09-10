package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import common.rich.RichT._
import models.{IOSong, Song}

object UrlPathUtils {
  private val Encoding = "UTF-8"
  private val EncodedPlus = "%2B"
  private val SpaceEncoding = "%20"

  /** A unique, URL-safe path of the song. */
  def encodePath(s: Song): String = {
    val path = s.file.path
    // For reasons which are beyond me, Play, being the piece of shit that it is, will try to decode %2B as
    // '+' (despite the documentation claiming that it shouldn't), which combined with the encoding of ' '
    // to '+' messes stuff up. The easiest way to solve this is by manually encoding ' ' to "%20" when a '+'
    // is present in the path.
    URLEncoder.encode(path, Encoding).mapIf(path.contains("+").const).to(_.replaceAll("\\+", SpaceEncoding))
  }

  def decode(s: String): String = {
    // Play converts %2B to '+' (see above), which is in turned decoded as ' '. To fix this bullshit, '+' is
    // manually converted back to "%2B" if there are "%20" tokens, which (presumably) means that '+' isn't
    // used for spaces.
    val fixedPath = s.mapIf(_ contains SpaceEncoding).to(_.replaceAll("\\+", EncodedPlus))
    URLDecoder.decode(fixedPath, Encoding)
  }

  // While one could potentially use JsString(path).parseJsonable[Song] or something to that effect,
  // the path isn't really a JSON value, and also it tightly couples the code to the specific Jsonable
  // implementation.
  def parseSong(path: String): IOSong = Song(parseFile(path))
  def parseFile(path: String): File = new File(UrlPathUtils.decode(path))
}
