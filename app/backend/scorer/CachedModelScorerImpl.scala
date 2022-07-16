package backend.scorer

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage}
import javax.inject.Inject
import models.Song

import scala.concurrent.ExecutionContext

import scalaz.Id.Id
import scalaz.OptionT
import common.rich.func.ToMoreMonadTransOps.toMoreMonadTransOps

import common.io.FileRef
import common.rich.RichFuture.richFuture

/**
* Works by first loading all entries from storage and caching them inside a map. Useful for scripts, as it has
* much lower answer latency, but obviously not that useful for a running server since it won't get updated.
*/
private class CachedModelScorerImpl @Inject()(
    artistScorer: ArtistScoreStorage,
    albumScorer: AlbumScoreStorage,
    reconcilableFactory: ReconcilableFactory,
    ec: ExecutionContext,
) extends CachedModelScorer {
  private implicit val iec: ExecutionContext = ec

  private lazy val albumScores: Map[(Artist, String), ModelScore] =
    albumScorer.loadAll.run.get
        .map(e => (e._1, e._2.toLowerCase) -> e._3)
        .toMap
  private lazy val artistScores: Map[Artist, ModelScore] =
    artistScorer.loadAll.run.get.toMap
  private val tempSongScore: Any => OptionT[Id, ModelScore] = _ => OptionT.none
  private val aux = new CompositeScorer[Id](
    tempSongScore,
    a => albumScores.get((a.artist, a.title.toLowerCase)).hoistId,
    a => artistScores.get(a.normalized).hoistId,
  )
  override def apply(a: Artist): Option[ModelScore] = artistScores.get(a.normalized)
  override def apply(a: Album): Option[ModelScore] = albumScores.get((a.artist, a.title.toLowerCase))
  override def apply(s: Song): Option[ModelScore] = aux(s).toModelScore
  // FIXME skipping song scores for now
  override def apply(f: FileRef): Option[ModelScore] =
    reconcilableFactory.toAlbum(f.parent).toOption.flatMap {album =>
      albumScores.get(album.artist.normalized -> album.title.toLowerCase)
          .orElse(artistScores.get(album.artist.normalized))
    }
}
