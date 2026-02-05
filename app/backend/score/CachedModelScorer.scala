package backend.score

import backend.recon._
import backend.score.storage.{AlbumScoreStorage, ArtistScoreStorage, TrackScoreStorage}
import com.google.inject.Inject

import scala.collection.Map
import scala.util.{Failure, Success, Try}

import cats.Id
import common.rich.func.kats.ToTransableOps.toHoistIdOps

import common.path.ref.FileRef
import common.rich.RichFuture.richFutureBlocking
import common.rich.RichT.richT
import common.rich.primitives.RichBoolean.richBoolean
import common.rich.primitives.RichEither.richEither

/**
 * Works by first loading all entries from storage and caching them inside a map. Useful for
 * scripts, as it has much lower answer latency, but in a running server its updates need to be
 * managed.
 */
private class CachedModelScorer @Inject() (
    artistScorer: ArtistScoreStorage,
    albumScorer: AlbumScoreStorage,
    songScorer: TrackScoreStorage,
    reconFactory: ReconcilableFactory,
) {
  private lazy val songScores: Map[YearlessTrack, ModelScore] =
    songScorer.loadAllScores.value.get.toMap
  private lazy val albumScores: Map[YearlessAlbum, ModelScore] =
    albumScorer.loadAllScores.value.get.toMap
  private lazy val artistScores: Map[Artist, ModelScore] = artistScorer.loadAll.value.get.toMap
  private val aux = new CompositeScorer[Id](
    explicitScore(_).toModelScore.hoistId,
    explicitScore(_).toModelScore.hoistId,
    explicitScore(_).toModelScore.hoistId,
  )
  def explicitScore(a: Artist): OptionalModelScore = artistScores.get(a).toOptionalModelScore
  def explicitScore(a: Album): OptionalModelScore =
    albumScores.get(a.toYearless).toOptionalModelScore
  def explicitScore(t: Track): OptionalModelScore =
    songScores.get(t.toYearless).toOptionalModelScore

  def tryAggregateScore(f: FileRef): Option[SourcedOptionalModelScore] = for {
    songTitle <- reconFactory.trySongInfo(f).|>(toOption(f, "song")).map(_._2)
    album <- {
      val albumAsTry = reconFactory.toAlbumFromFileOnly(f.parent).toErrorTry
      toOption(f, "album")(albumAsTry).map(_.toYearless)
    }
  } yield {
    val artist = album.artist
    songScores
      .get(YearlessTrack(songTitle, album))
      .map(SourcedOptionalModelScore.Scored(_, ScoreSource.Song))
      .orElse(albumScores.get(album).map(SourcedOptionalModelScore.Scored(_, ScoreSource.Album)))
      .orElse(artistScores.get(artist).map(SourcedOptionalModelScore.Scored(_, ScoreSource.Artist)))
      .getOrElse(SourcedOptionalModelScore.Default)
  }

  def fullInfo(t: Track): Id[FullInfoScore] = aux(t)

  private def toOption[A](fileRef: FileRef, subject: String)(t: Try[A]): Option[A] = t match {
    case Failure(exception) =>
      if (fileRef.path.contains("Classical").isFalse)
        scribe.trace(
          s"Could not parse <$subject> from <$fileRef> because: <${exception.getMessage}>",
        )
      None
    case Success(value) => Some(value)
  }
}
