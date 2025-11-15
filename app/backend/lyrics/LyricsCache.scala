package backend.lyrics

import backend.lyrics.LyricsUrl.ManualEmpty
import backend.lyrics.retrievers._
import backend.storage.OnlineRetrieverCacher
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.rich.RichFuture.RichTryFuture

/** Handles persisting lyrics when retrieved from external sources. */
private class LyricsCache @Inject() (
    ec: ExecutionContext,
    activeRetriever: ActiveRetriever,
    passiveParsers: PassiveParser,
    lyricsStorage: LyricsStorage,
) {
  private implicit val iec: ExecutionContext = ec
  private val cache = new OnlineRetrieverCacher[Song, Lyrics](
    lyricsStorage,
    s =>
      activeRetriever(s)
        .listenError(e => scribe.error(s"Failure to fetch lyrics for <${s.file.path}>", e))
        .mapEitherMessage {
          case RetrievedLyricsResult.RetrievedLyrics(l) => Right(l)
          case _ => Left("No lyrics retrieved :(")
        },
  )
  def find(s: Song): Future[Lyrics] = cache(s).fromTry
  def parse(url: Url, s: Song): Future[RetrievedLyricsResult] =
    if (passiveParsers.doesUrlMatchHost(url))
      passiveParsers.parse(url, s).flatTap {
        case x @ RetrievedLyricsResult.RetrievedLyrics(l) => cache.replace(s, l).value as x
        case err @ RetrievedLyricsResult.Error(e) =>
          scribe.error(s"Failed to parse <$url>; error: <$e>")
          Future.successful(err)
        case nl @ RetrievedLyricsResult.NoLyrics =>
          scribe.error(s"Couldn't extract the lyrics from <$url>")
          Future.successful(nl)
      }
    else
      Future.successful(RetrievedLyricsResult.Error.unsupportedHost(url))

  def setInstrumentalSong(s: Song): Future[Instrumental] = {
    val instrumental = Instrumental("Manual override", ManualEmpty)
    cache.replace(s, instrumental).value as instrumental
  }
}
