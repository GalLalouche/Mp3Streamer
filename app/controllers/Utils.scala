package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import backend.configs.RealConfig
import backend.logging._
import common.rich.path.RichFile._
import models.{Poster, Song}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.AnyContent
import search.ModelsJsonable.SongJsonifier
import common.RichJson._

import scala.concurrent.ExecutionContext

private object Utils {
  implicit val config = new RealConfig {
    override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    override implicit val logger: Logger =
      new CompositeLogger(new ConsoleLogger with FilteringLogger, new DirectoryLogger(rootDirectory addSubDir "log"))
  }
  def toJson(s: Song): JsObject = {
    // TODO lenses?
    val $ = SongJsonifier.jsonify(s)
    $ + ("file" -> URLEncoder.encode($.str("file"), "UTF-8")) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s).path)) +
        (s.file.extension -> JsString("/stream/download/" + URLEncoder.encode(s.file.path, "UTF-8")))
  }
  def parseSong(path: String): Song = Song(parseFile(path))
  def parseFile(path: String): File = new File(URLDecoder.decode(path, "UTF-8"))

  def getStringFromBody(a: AnyContent): String = a.asFormUrlEncoded.get.keys.head
}
