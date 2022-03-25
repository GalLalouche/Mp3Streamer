package backend.scorer

import backend.recon.{Album, Artist}
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage, CompositeStorageScorer, SongScoreStorage, StorageScorer}
import models.Song
import net.codingwell.scalaguice.ScalaModule

object ScorerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[StorageScorer[Artist]].to[ArtistScoreStorage]
    bind[StorageScorer[Album]].to[AlbumScoreStorage]
    bind[StorageScorer[Song]].to[SongScoreStorage]
    bind[ModelScorer].to[CompositeStorageScorer]
  }
}
