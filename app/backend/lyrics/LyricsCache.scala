package backend.lyrics

import backend.Url
import backend.logging.Logger
import backend.lyrics.retrievers.{RetrievedLyricsResult, _}
import backend.storage.OnlineRetrieverCacher
import com.google.inject.Guice
import javax.inject.Inject
import models.{IOSong, Song}

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.option.optionInstance
import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor._
import common.rich.func.ToMoreFoldableOps._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._

import common.rich.RichT._

// TODO test
private class LyricsCache @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    defaultArtistInstrumental: InstrumentalArtist,
    htmlComposites: CompositeHtmlRetriever,
    @CompositePassiveParser passiveParsers: PassiveParser,
    @CompositeAlbumParser albumParsers: HtmlRetriever,
    lyricsStorage: LyricsStorage,
) {
  private implicit val iec: ExecutionContext = ec
  private val firstDefaultRetrievers = DefaultClassicalInstrumental
  private val lastDefaultRetrievers = defaultArtistInstrumental
  private val allComposite = new CompositeLyricsRetriever(
    logger, Vector(htmlComposites, albumParsers, lastDefaultRetrievers, firstDefaultRetrievers))
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](
    lyricsStorage,
    allComposite(_) mapEitherMessage {
      case RetrievedLyricsResult.RetrievedLyrics(l) => \/-(l)
      case _ => -\/("No lyrics retrieved :(")
    }
  )
  def find(s: Song): Future[Lyrics] = cache(s)
  def parse(url: Url, s: Song): Future[RetrievedLyricsResult] = {
    def aux(parser: PassiveParser): Future[RetrievedLyricsResult] = parser.parse(url, s).listen {
      case RetrievedLyricsResult.RetrievedLyrics(l) => cache.forceStore(s, l)
      case _ => ()
    }
    def check(pp: PassiveParser): Option[PassiveParser] = pp.optFilter(_.doesUrlMatchHost(url))
    check(htmlComposites)
        .orElse(check(passiveParsers))
        .mapHeadOrElse(aux, Future.successful(RetrievedLyricsResult.Error.unsupportedHost(url)))
  }

  def setInstrumentalSong(s: Song): Future[Instrumental] = {
    val instrumental = Instrumental("Manual override")
    cache.forceStore(s, instrumental).run >| instrumental
  }
  def setInstrumentalArtist(s: Song): Future[Instrumental] = defaultArtistInstrumental add s
}

object LyricsCache {
  import java.io.File

  import backend.module.CleanModule
  import net.codingwell.scalaguice.InjectorExtensions._

  import common.rich.RichFuture._

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector CleanModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val s = IOSong.read(new File("""D:\Media\Music\Rock\Punk\Heartsounds\2013 Internal Eyes\01 - A Total Separation of Self.mp3"""))
    val $ = injector.instance[LyricsCache]
    println($.find(s).get)
  }
}
