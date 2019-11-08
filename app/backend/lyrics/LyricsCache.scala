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
import scalaz.std.scalaFuture.futureInstance
import scalaz.syntax.functor._
import common.rich.func.ToMoreFunctorOps._
import common.rich.func.ToMoreMonadErrorOps._

private class LyricsCache @Inject()(
    ec: ExecutionContext,
    logger: Logger,
    defaultArtistInstrumental: InstrumentalArtist,
    htmlComposites: CompositeHtmlRetriever,
    lyricsStorage: LyricsStorage,
) {
  private implicit val iec: ExecutionContext = ec
  private val firstDefaultRetrievers = DefaultClassicalInstrumental
  private val lastDefaultRetrievers = defaultArtistInstrumental
  private val allComposite = new CompositeLyricsRetriever(
    ec, logger, Vector(htmlComposites, lastDefaultRetrievers, firstDefaultRetrievers))
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](
    lyricsStorage,
    allComposite(_) mapEitherMessage {
      case RetrievedLyricsResult.RetrievedLyrics(l) => \/-(l)
      case _ => -\/("No lyrics retrieved :(")
    }
  )
  def find(s: Song): Future[Lyrics] = cache(s)
  def parse(url: Url, s: Song): Future[RetrievedLyricsResult] = htmlComposites.parse(url, s)
      .listen {
        case RetrievedLyricsResult.RetrievedLyrics(l) => cache.forceStore(s, l)
        case _ => ()
      }

  def setInstrumentalSong(s: Song): Future[Instrumental] = {
    val instrumental = Instrumental("Manual override")
    cache.forceStore(s, instrumental) >| instrumental
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
