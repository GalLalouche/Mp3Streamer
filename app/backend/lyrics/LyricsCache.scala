package backend.lyrics

import backend.lyrics.LyricsUrl.ManualEmpty
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreFoldableOps._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.{-\/, \/-}
import scalaz.Scalaz.ToBindOps
import scalaz.std.option.optionInstance
import scalaz.syntax.functor.ToFunctorOps

import common.rich.RichFuture.RichTryFuture
import common.rich.RichT._

// TODO test
private class LyricsCache @Inject() (
    ec: ExecutionContext,
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
    Vector(htmlComposites, albumParsers, lastDefaultRetrievers, firstDefaultRetrievers),
  )
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](
    lyricsStorage,
    allComposite(_).mapEitherMessage {
      case RetrievedLyricsResult.RetrievedLyrics(l) => \/-(l)
      case _ => -\/("No lyrics retrieved :(")
    },
  )
  def find(s: Song): Future[Lyrics] = cache(s).fromTry
  def parse(url: Url, s: Song): Future[RetrievedLyricsResult] = {
    def aux(parser: PassiveParser): Future[RetrievedLyricsResult] = parser.parse(url, s) >>! {
      case RetrievedLyricsResult.RetrievedLyrics(l) => cache.replace(s, l).run
      case _ => Future.successful(())
    }
    def check(pp: PassiveParser): Option[PassiveParser] = pp.optFilter(_.doesUrlMatchHost(url))
    check(htmlComposites)
      .orElse(check(passiveParsers))
      .mapHeadOrElse(aux, Future.successful(RetrievedLyricsResult.Error.unsupportedHost(url)))
  }

  def setInstrumentalSong(s: Song): Future[Instrumental] = {
    val instrumental = Instrumental("Manual override", ManualEmpty)
    cache.replace(s, instrumental).run >| instrumental
  }
  def setInstrumentalArtist(s: Song): Future[Instrumental] = defaultArtistInstrumental.add(s)
}
