package backend.lyrics

import backend.lyrics.retrievers.{InstrumentalArtist, RetrievedLyricsResult}
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import models.Song

import scala.concurrent.Future

/** Unified backend for [[LyricsFormatter]]. */
private class LyricsBackend @Inject() (
    lc: LyricsCache,
    instrumentalArtist: InstrumentalArtist,
) {
  def find(s: Song): Future[Lyrics] = lc.find(s)
  def parse(url: Url, s: Song): Future[RetrievedLyricsResult] = lc.parse(url, s)
  def setInstrumentalSong(s: Song): Future[Instrumental] = lc.setInstrumentalSong(s)
  def setInstrumentalArtist(s: Song): Future[Instrumental] = instrumentalArtist.add(s)
}
