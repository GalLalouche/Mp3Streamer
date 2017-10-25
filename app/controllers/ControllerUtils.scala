package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import backend.configs.RealConfig
import backend.logging._
import common.rich.RichT._
import common.rich.path.RichFile._
import models.{IOSong, ModelJsonable, Poster, Song}
import play.api.libs.json.{JsObject, JsString}

import scala.concurrent.ExecutionContext

object ControllerUtils {
  implicit val config: RealConfig = new RealConfig {
    override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    override implicit val logger: Logger =
      new CompositeLogger(new ConsoleLogger with FilteringLogger {
        setCurrentLevel(LoggingLevel.Verbose)
      }, new DirectoryLogger()(this))
  }
  import ModelJsonable._

  // Visible for testing
  def encode(s: Song): String = {
    val path = s.file.path
    // For reasons which are beyond me, Play, being the piece of shit that it is, will try to decode %2B as '+' (despite
    // the documentation claiming that it shouldn't), which combined with the encoding of ' ' to '+' messes stuff up.
    // The easiest way to solve this is by manually encoding ' ' to "%20" when a '+' is present in the path.
    URLEncoder.encode(path, "UTF-8").mapIf(path.contains("+").const).to(_.replaceAll("\\+", "%20"))
  }

  def toJson(s: Song): JsObject = s.jsonify.as[JsObject] +
      ("file" -> JsString(encode(s))) +
      ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
      (s.file.extension -> JsString("/stream/download/" + encode(s)))
  def parseSong(path: String): IOSong = Song(parseFile(path))
  def parseFile(path: String): File = {
    // Play converts %2B to '+' (see above), which is in turned decoded as ' '. To fix this bullshit, '+' is manually
    // converted back to "%2B" if there are "%20" tokens, which (presumably) means that '+' isn't used for spaces.
    val fixedPath = path.mapIf(_ contains "%20").to(_.replaceAll("\\+", "%2B"))
    new File(URLDecoder.decode(fixedPath, "UTF-8"))
  }
}
