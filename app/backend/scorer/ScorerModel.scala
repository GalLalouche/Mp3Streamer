package backend.scorer

import backend.recon.{Album, Artist, Track}
import backend.recon.Reconcilable._
import backend.scorer.storage.StorageScorer
import com.google.inject.Inject
import models.Song

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.Scalaz.ToBindOpsUnapply

private class ScorerModel @Inject() (
    trackScorer: StorageScorer[Track],
    albumScorer: StorageScorer[Album],
    artistScorer: StorageScorer[Artist],
    cachedModelScorerState: CachedModelScorerState,
    ec: ExecutionContext,
) extends FullInfoModelScorer {
  private implicit val iec: ExecutionContext = ec
  override def apply(s: Song): Future[FullInfoScore] = aux(s.track)

  private val aux =
    new CompositeScorer[Future](trackScorer.apply, albumScorer.apply, artistScorer.apply)

  override def updateSongScore(song: Song, score: OptionalModelScore): Future[Unit] =
    trackScorer.updateScore(song.track, score) >> cachedModelScorerState.update()
  override def updateAlbumScore(song: Song, score: OptionalModelScore): Future[Unit] =
    albumScorer.updateScore(song.release, score) >> cachedModelScorerState.update()
  override def updateArtistScore(song: Song, score: OptionalModelScore): Future[Unit] =
    artistScorer.updateScore(song.artist, score) >> cachedModelScorerState.update()
}
