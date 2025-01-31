package backend.scorer

import javax.inject.Inject

import backend.recon.{Album, Artist, ReconcilableFactory, Track, YearlessAlbum, YearlessTrack}
import backend.recon.Reconcilable.SongExtractor
import backend.scorer.storage.{AlbumScoreStorage, ArtistScoreStorage, TrackScoreStorage}
import models.MusicFinder

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

import common.rich.func.ToTransableOps.toHoistIdOps
import scalaz.Id.Id

import common.io.FileRef
import common.rich.RichFuture.richFuture
import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean

/**
 * Works by first loading all entries from storage and caching them inside a map. Useful for
 * scripts, as it has much lower answer latency, but obviously not that useful for a running server
 * since it won't get updated.
 */
private class CachedModelScorerImpl @Inject() (
    artistScorer: ArtistScoreStorage,
    albumScorer: AlbumScoreStorage,
    songScorer: TrackScoreStorage,
    reconFactory: ReconcilableFactory,
    mf: MusicFinder,
    ec: ExecutionContext,
) extends CachedModelScorer {
  private implicit val iec: ExecutionContext = ec

  private lazy val songScores: Map[YearlessTrack, ModelScore] = songScorer.loadAll.run.get.toMap
  private lazy val albumScores: Map[YearlessAlbum, ModelScore] = albumScorer.loadAll.run.get.toMap
  private lazy val artistScores: Map[Artist, ModelScore] = artistScorer.loadAll.run.get.toMap
  private val aux = new CompositeScorer[Id](
    explicitScore(_).toModelScore.hoistId,
    explicitScore(_).toModelScore.hoistId,
    explicitScore(_).toModelScore.hoistId,
  )
  override def explicitScore(a: Artist): OptionalModelScore =
    artistScores.get(a).toOptionalModelScore
  override def explicitScore(a: Album): OptionalModelScore =
    albumScores.get(a.toYearless).toOptionalModelScore
  override def explicitScore(t: Track): OptionalModelScore =
    songScores.get(t.toYearless).toOptionalModelScore

  override def aggregateScore(f: FileRef): OptionalModelScore = {
    lazy val id3Song = mf.parseSong(f)
    val songTitle =
      reconFactory.songTitle(f).|>(toOption(f, "song")).getOrElse(id3Song.title)
    val album: YearlessAlbum =
      reconFactory.toAlbum(f.parent).|>(toOption(f, "album")).getOrElse(id3Song.release).toYearless
    val artist = album.artist
    songScores
      .get(YearlessTrack(songTitle, album))
      .orElse(albumScores.get(album))
      .orElse(artistScores.get(artist))
      .toOptionalModelScore
  }

  override def fullInfo(t: Track) = aux(t)

  private def toOption[A](fileRef: FileRef, subject: String)(t: Try[A]): Option[A] = t match {
    case Failure(exception) =>
      if (fileRef.path.contains("Classical").isFalse)
        scribe.debug(
          s"Could not parse <$subject> from <$fileRef> because: <${exception.getMessage}>",
        )
      None
    case Success(value) => Some(value)
  }
}
