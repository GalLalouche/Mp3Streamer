package controllers

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import backend.configs.RealConfig
import backend.logging.{CompositeLogger, ConsoleLogger, DirectoryLogger, FilteringLogger, Logger, LoggingLevel}
import com.google.common.annotations.VisibleForTesting
import com.google.inject.{Guice, Provides}
import com.google.inject.util.Modules
import common.RichJson._
import common.io.{DirectoryRef, RootDirectory}
import common.json.{Jsonable, JsonableOverrider, OJsonableOverrider}
import common.rich.RichT._
import common.rich.path.RichFile._
import models.{IOSong, Poster, Song}
import models.ModelJsonable._
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.Request

import scala.concurrent.ExecutionContext

object ControllerUtils {
  implicit lazy val config: RealConfig = new RealConfig {self =>
    override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
    override val module = Modules.combine(super.module, new ScalaModule {
      override def configure(): Unit = {
        bind[ExecutionContext] toInstance ec
      }
      
      @Provides
      private def provideLogger(@RootDirectory rootDirectory: DirectoryRef): Logger = new CompositeLogger(
        new ConsoleLogger with FilteringLogger {setCurrentLevel(LoggingLevel.Verbose)},
        new DirectoryLogger(rootDirectory)(self),
      )
    })
    override def injector = Guice createInjector module
  }

  private val Encoding = "UTF-8"
  private val EncodedPlus = "%2B"
  private val SpaceEncoding = "%20"

  // TODO move to its own file
  /** A unique, URL-safe path of the song. */
  def encodePath(s: Song): String = {
    val path = s.file.path
    // For reasons which are beyond me, Play, being the piece of shit that it is, will try to decode %2B as '+' (despite
    // the documentation claiming that it shouldn't), which combined with the encoding of ' ' to '+' messes stuff up.
    // The easiest way to solve this is by manually encoding ' ' to "%20" when a '+' is present in the path.
    URLEncoder.encode(path, Encoding).mapIf(path.contains("+").const).to(_.replaceAll("\\+", SpaceEncoding))
  }
  @VisibleForTesting
  private[controllers] def decode(s: String): String = {
    // Play converts %2B to '+' (see above), which is in turned decoded as ' '. To fix this bullshit, '+' is manually
    // converted back to "%2B" if there are "%20" tokens, which (presumably) means that '+' isn't used for spaces.
    val fixedPath = s.mapIf(_ contains SpaceEncoding).to(_.replaceAll("\\+", EncodedPlus))
    URLDecoder.decode(fixedPath, Encoding)
  }

  implicit val songJsonable: Jsonable[Song] = JsonableOverrider[Song](new OJsonableOverrider[Song] {
    override def jsonify(s: Song, original: => JsObject) = original +
        ("file" -> JsString(encodePath(s))) +
        ("poster" -> JsString("/posters/" + Poster.getCoverArt(s.asInstanceOf[IOSong]).path)) +
        (s.file.extension -> JsString("/stream/download/" + encodePath(s)))
    override def parse(obj: JsObject, unused: => Song) =
      SongJsonifier.parse(obj + ("file" -> JsString(decode(obj str "file"))))
  })

  // While one could potentially use JsString(path).parseJsonable[Song] or something to that effect,
  // the path isn't really a JSON value, and also it tightly couples the code to the specific Jsonable
  // implementation.
  def parseSong(path: String): IOSong = Song(parseFile(path))
  def parseFile(path: String): File = new File(decode(path))

  def encodeMp3(request: Request[_]): Boolean =
    request.headers("User-Agent").contains("Chrome") || request.headers.get("Referer").exists(_ endsWith "mp3")
}
