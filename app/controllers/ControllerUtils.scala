package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import backend.configs.RealConfig
import backend.logging._
import com.google.common.annotations.VisibleForTesting
import common.json.CovariantJsonable
import common.rich.RichT._
import common.rich.path.RichFile._
import models.ModelJsonable._
import models.{IOSong, Poster, Song}
import play.api.libs.json.{JsString, JsValue}

import scala.concurrent.ExecutionContext

object ControllerUtils {
  implicit lazy val config: RealConfig = new RealConfig {
    override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    override implicit val logger: Logger =
      new CompositeLogger(new ConsoleLogger with FilteringLogger {
        setCurrentLevel(LoggingLevel.Verbose)
      }, new DirectoryLogger()(this))
  }

  private val Encoding = "UTF-8"
  private val EncodedPlus = "%2B"
  private val SpaceEncoding = "%20"

  @VisibleForTesting
  def encode(s: Song): String = {
    val path = s.file.path
    // For reasons which are beyond me, Play, being the piece of shit that it is, will try to decode %2B as '+' (despite
    // the documentation claiming that it shouldn't), which combined with the encoding of ' ' to '+' messes stuff up.
    // The easiest way to solve this is by manually encoding ' ' to "%20" when a '+' is present in the path.
    URLEncoder.encode(path, Encoding).mapIf(path.contains("+").const).to(_.replaceAll("\\+", SpaceEncoding))
  }

  implicit object songJsonable extends CovariantJsonable[Song, IOSong] {
    override def jsonify(s: Song): JsValue = SongJsonifier.jsonify(s) +
        ("file" -> JsString(encode(s))) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
        (s.file.extension -> JsString("/stream/download/" + encode(s)))
    override def parse(a: JsValue): IOSong = Song(parseFile(a.as[JsString].value))
  }

  def parseFile(path: String): File = {
    // Play converts %2B to '+' (see above), which is in turned decoded as ' '. To fix this bullshit, '+' is manually
    // converted back to "%2B" if there are "%20" tokens, which (presumably) means that '+' isn't used for spaces.
    val fixedPath = path.mapIf(_ contains SpaceEncoding).to(_.replaceAll("\\+", EncodedPlus))
    new File(URLDecoder.decode(fixedPath, Encoding))
  }
}
