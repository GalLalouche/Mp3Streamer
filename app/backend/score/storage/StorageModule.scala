package backend.score.storage

import backend.recon.{Album, Artist, Track}
import net.codingwell.scalaguice.ScalaModule

private[score] object StorageModule extends ScalaModule {
  override def configure(): Unit = {
    bind[StorageScorer[Artist]].to[ArtistScoreStorage]
    bind[StorageScorer[Album]].to[AlbumScoreStorage]
    bind[StorageScorer[Track]].to[TrackScoreStorage]
  }
}
