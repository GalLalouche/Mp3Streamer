package backend.scorer.storage

import javax.inject.Inject

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable._
import backend.scorer.{CachedModelScorerState, CompositeScorer, FullInfoModelScorer, FullInfoScore, ModelScore, OptionalModelScore}
import backend.scorer.FullArtistScores.{AlbumScore, ArtistScore, SongScore}
import backend.scorer.utils.Scoreable.ScoreableImpl
import backend.scorer.utils.ScoreFileEditor
import com.google.common.annotations.VisibleForTesting
import mains.fixer.StringFixer
import models.{AlbumTitle, ArtistFactory, ArtistFinder, MusicFinder, Song, SongTitle, TrackNumber}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

import common.rich.func.BetterFutureInstances._
import monocle.Monocle.toApplyLensOps
import scalaz.{Applicative, ListT}
import scalaz.Scalaz.{ToBindOpsUnapply, ToFunctorOps}
import scalaz.std.option.optionInstance

import common.io.{DirectoryRef, FileRef}
import common.rich.RichT.richT
import common.rich.RichTuple._
import common.rich.primitives.RichOption.richOption

/** Scores a song by trying multiple sources, from most specific score to least specific. */
private[scorer] class CompositeStorageScorer @Inject() (
    trackScorer: TrackScoreStorage,
    albumScorer: AlbumScoreStorage,
    artistScorer: StorageScorer[Artist],
    cachedModelScorerState: CachedModelScorerState,
    ec: ExecutionContext,
    artistFinder: ArtistFinder,
    mf: MusicFinder,
    fixer: StringFixer,
    rc: ReconcilableFactory,
    artistFactory: ArtistFactory,
    scoreableImpl: ScoreableImpl,
    scoreEditor: ScoreFileEditor,
) extends FullInfoModelScorer {
  override def apply(s: Song): Future[FullInfoScore] = aux(s.track)

  private implicit val iec: ExecutionContext = ec
  private val aux =
    new CompositeScorer[Future](trackScorer.apply, albumScorer.apply, artistScorer.apply)

  override def updateSongScore(song: Song, score: OptionalModelScore) =
    trackScorer.updateScore(song.track, score) >> cachedModelScorerState.update()
  override def updateAlbumScore(song: Song, score: OptionalModelScore) =
    albumScorer.updateScore(song.release, score) >> cachedModelScorerState.update()
  override def updateArtistScore(song: Song, score: OptionalModelScore) =
    artistScorer.updateScore(song.artist, score) >> cachedModelScorerState.update()

  override def openScoreFile(song: Song): Future[Unit] =
    scoreEditor(Vector(artistFactory.fromSong(song)))(scoreableImpl.artist)

  @VisibleForTesting private[storage] def getArtistScores(a: Artist): Future[ArtistScore] = {
    val artistDir = artistFinder(a.name).getOrThrow(new IllegalArgumentException(s"<$a> not found"))
    val albumDirs = mf.albumDirs(Vector(artistDir.asInstanceOf[mf.S#D]))
    val albums: ListT[Future, (DirectoryRef, Album)] = ListT(
      Future(
        albumDirs
          .flatMap(a =>
            rc.toAlbum(a)
              .|>(tryToOptionWithLogging(s"Could not build album out of directory <$a>"))
              .strengthL(a),
          )
          .toList,
      ),
    )
    val songs: Future[Seq[(Album, Seq[(FileRef, TrackNumber, SongTitle)])]] =
      albums.map(_.modifyFirst(mf.getSongFilesInDir(_).map(_.:->(rc.songInfo).flatten)).swap).run
    Applicative[Future].apply4(
      artistScorer(a).run.map(_.toOptionalModelScore),
      songs,
      albumScorer.allForArtist(a).map(_.toMap),
      trackScorer.allForArtist(a).map(_.map(e => (e._1, e._2) -> e._3).toMap),
    )(go(a))
  }

  // TODO move to somewhere common
  private def tryToOptionWithLogging[A](msg: => String)(t: Try[A]): Option[A] = t match {
    case Failure(e) =>
      scribe.warn(msg, e)
      None
    case Success(value) => Some(value)
  }

  private def go(
      artist: Artist,
  )(
      artistScore: OptionalModelScore,
      songsByAlbum: Seq[(Album, Seq[(FileRef, TrackNumber, SongTitle)])],
      albumScores: Map[AlbumTitle, ModelScore],
      songScores: Map[(AlbumTitle, SongTitle), ModelScore],
  ): ArtistScore = ArtistScore(
    artist = artist.&|->(Artist.name).modify(fixer),
    score = artistScore,
    albumScores = songsByAlbum.map { case (album, songs) =>
      AlbumScore(
        album,
        albumScores.get(album.title).toOptionalModelScore,
        songs.map { case (fileRef, trackNumber, songTitle) =>
          SongScore(
            fileRef,
            trackNumber,
            songTitle,
            songScores.get(album.title.toLowerCase, songTitle.toLowerCase).toOptionalModelScore,
          )
        },
      )
    },
  )
}
