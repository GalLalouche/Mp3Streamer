package backend.scorer.utils

import javax.inject.Inject

import backend.recon.ReconcilableFactory
import backend.scorer.{CachedModelScorer, OptionalModelScore}
import backend.scorer.utils.ArtistMassScorer.Update
import backend.scorer.utils.Scoreable.ScoreableImpl
import models.{Artist, ArtistFactory, Genre, GenreFinder}

import scala.concurrent.ExecutionContext

import scalaz.Scalaz.{ToBindOpsUnapply, ToFunctorOpsUnapply}
import scalaz.State
import scalaz.std.vector.vectorInstance
import scalaz.syntax.traverse.ToTraverseOps

import common.{OrgModeWriter, OrgModeWriterMonad}
import common.OrgModeWriterMonad.OrgModeWriterMonad
import common.io.IODirectory
import common.rich.RichT._

/**
 * Creates an .org file for faster updating of artists. See [[ScoreParser]] for the parser of the
 * output file.
 */
// REMAINING Support for albums, songs
private class ArtistMassScorer @Inject() (
    scorer: CachedModelScorer,
    reconcilableFactory: ReconcilableFactory,
    enumGenreFinder: GenreFinder,
    ec: ExecutionContext,
    artistFactory: ArtistFactory,
    impl: ScoreableImpl,
) {
  private implicit val iec: ExecutionContext = ec
  private implicit val artistScoreable: Scoreable[Artist] = impl.artist
  def go(update: Update): Seq[String] = {
    def goGenre(g: Genre, artists: Iterable[Artist]): OrgModeWriterMonad = {
      val filteredArtists: Iterable[Artist] = for {
        artist <- artists
        score = scorer.explicitScore(artist.toRecon)
        if update.filterScore(score)
      } yield artist
      if (filteredArtists.isEmpty) // Don't add genres without artists
        State.init[OrgModeWriter].void
      else
        OrgModeWriterMonad.append(g.name) >> MassScorer(filteredArtists.toVector.sortBy(_.name))
    }

    reconcilableFactory.artistDirectories
      .groupBy(e => enumGenreFinder.apply(e.asInstanceOf[IODirectory]))
      .mapValues(_.map(artistFactory.fromDir(_)))
      .toVector
      .sortBy(_._1)
      .traverse(Function.tupled(goGenre))
      .void
      .|>(OrgModeWriterMonad.run)
      .lines
  }
}

private object ArtistMassScorer {
  sealed trait Update {
    def filterScore(s: OptionalModelScore): Boolean = this match {
      case Update.NoScore => s == OptionalModelScore.Default
      case Update.All => true
    }
  }
  object Update {
    case object NoScore extends Update
    case object All extends Update
  }
}
