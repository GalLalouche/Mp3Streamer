package backend.lyrics

import backend.lyrics.retrievers.InstrumentalArtistStorage
import backend.recon.{Artist, ArtistReconStorage, StoredReconResult}
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.{ToBindOps, ToFunctorOps}

class LyricsBridge @Inject() (
    lyricsStorage: LyricsStorage,
    artistStorage: ArtistReconStorage,
    instrumentalArtistStorage: InstrumentalArtistStorage,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def setup(song: Song): Future[Unit] =
    artistStorage.utils.clearOrCreateTable() >>
      artistStorage.store(Artist(song.artistName), StoredReconResult.StoredNull) >>
      lyricsStorage.utils.createTable() >>
      instrumentalArtistStorage.utils.createTable().void
  def store(song: Song, source: String, html: String, url: Url): Future[Unit] =
    lyricsStorage.store(song, HtmlLyrics(source, html, LyricsUrl.Url(url)))
}
