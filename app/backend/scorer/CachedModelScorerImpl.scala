package backend.scorer

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}
import scalaz.Id.Id

import backend.logging.Logger
import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage, SongScoreStorage}
import common.io.FileRef
import common.rich.func.ToMoreMonadTransOps.toMoreMonadTransOps
import common.rich.RichFuture.richFuture
import common.rich.RichT.richT
import models.{MusicFinder, Song}

/**
 * Works by first loading all entries from storage and caching them inside a map. Useful for
 * scripts, as it has much lower answer latency, but obviously not that useful for a running server
 * since it won't get updated.
 */
private class CachedModelScorerImpl @Inject() (
    artistScorer: ArtistScoreStorage,
    albumScorer: AlbumScoreStorage,
    songScorer: SongScoreStorage,
    reconcilableFactory: ReconcilableFactory,
    mf: MusicFinder,
    ec: ExecutionContext,
    logger: Logger,
) extends CachedModelScorer {
  private implicit val iec: ExecutionContext = ec

  type SongTitle = String
  type AlbumTitle = String
  private lazy val songScores: Map[(Artist, AlbumTitle, SongTitle), ModelScore] =
    songScorer.loadAll.run.get.map(e => (e._1, e._2.toLowerCase, e._3.toLowerCase) -> e._4).toMap
  private lazy val albumScores: Map[(Artist, AlbumTitle), ModelScore] =
    albumScorer.loadAll.run.get.map(e => (e._1, e._2.toLowerCase) -> e._3).toMap
  private lazy val artistScores: Map[Artist, ModelScore] =
    artistScorer.loadAll.run.get.toMap
  private val aux = new CompositeScorer[Id](
    // FIXME This whole normalize BS should really stop :\ The class itself should always be normalized, or not.
    s =>
      songScores.get((s.artist.normalized, s.albumName.toLowerCase, s.title.toLowerCase)).hoistId,
    a => albumScores.get((a.artist.normalized, a.title.toLowerCase)).hoistId,
    a => artistScores.get(a.normalized).hoistId,
  )
  override def apply(a: Artist): Option[ModelScore] = artistScores.get(a.normalized)
  override def apply(a: Album): Option[ModelScore] =
    albumScores.get((a.artist.normalized, a.title.toLowerCase))
  override def apply(s: Song): Option[ModelScore] = fullInfo(s).toModelScore

  override def apply(f: FileRef): Option[ModelScore] = {
    lazy val id3Song = mf.parseSong(f)
    val songTitle =
      reconcilableFactory.songTitle(f).|>(toOption(f, "song")).getOrElse(id3Song.title)
    val album: Album =
      reconcilableFactory.toAlbum(f.parent).|>(toOption(f, "album")).getOrElse(id3Song.release)
    val albumTitle = album.title
    val artist = album.artist.normalized
    songScores
      .get((artist, albumTitle, songTitle))
      .orElse(albumScores.get((artist, albumTitle)))
      .orElse(artistScores.get(artist))
  }

  override def fullInfo(s: Song) = aux(s)

  private def toOption[A](fileRef: FileRef, subject: String)(t: Try[A]): Option[A] = t match {
    case Failure(exception) =>
      logger.debug(s"Could not parse <$subject> from <$fileRef> because: <${exception.getMessage}>")
      None
    case Success(value) => Some(value)
  }
}
