package backend.scorer

import backend.recon.{Album, Artist}
import javax.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

import common.rich.func.BetterFutureInstances._

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private class CompositeStorageScorer @Inject()(
    songScorer: StorageScorer[Song],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    ec: ExecutionContext,
) extends ModelScorer {
  private implicit val iec: ExecutionContext = ec
  private val aux = new CompositeScorer[Future](
    songScorer.apply,
    albumScorer.apply,
    artistScorer.apply
  )
  override def apply(s: Song): Future[ModelScorer.SongScore] = aux(s)
  override def updateSongScore(song: Song, score: ModelScore) = songScorer.store(song, score)
  override def updateAlbumScore(album: Album, score: ModelScore) = albumScorer.store(album, score)
  override def updateArtistScore(artist: Artist, score: ModelScore) = artistScorer.store(artist, score)
}
