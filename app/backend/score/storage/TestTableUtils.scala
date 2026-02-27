package backend.score.storage

import backend.recon.{Album, Artist, Track}
import jakarta.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.catsSyntaxApplyOps

class TestTableUtils @Inject() private (
    artistScorer: StorageScorer[Artist],
    albumScorer: StorageScorer[Album],
    trackScorer: StorageScorer[Track],
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def clearAll(): Future[_] = trackScorer.clear() *> albumScorer.clear() *> artistScorer.clear()
}
