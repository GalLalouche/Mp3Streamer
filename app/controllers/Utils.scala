package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import backend.configs.RealConfig
import backend.logging._
import common.rich.path.RichFile._
import models.{IOSong, Poster, Song}
import play.api.libs.json.{JsObject, JsString}
import backend.search.ModelJsonable

import scala.concurrent.ExecutionContext

object Utils {
  implicit val config: RealConfig = new RealConfig {
    override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    override implicit val logger: Logger =
      new CompositeLogger(new ConsoleLogger with FilteringLogger {
        setCurrentLevel(LoggingLevel.Verbose)
      }, new DirectoryLogger()(this))
  }
  import ModelJsonable._
  def toJson(s: Song): JsObject = s.jsonify +
      ("file" -> JsString(URLEncoder.encode(s.file.path, "UTF-8"))) +
      ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
      (s.file.extension -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  def parseSong(path: String): IOSong = Song(parseFile(path))
  def parseFile(path: String): File = new File(URLDecoder.decode(path, "UTF-8"))
}
