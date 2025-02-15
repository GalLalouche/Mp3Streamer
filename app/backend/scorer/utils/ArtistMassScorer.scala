package backend.scorer.utils

import javax.inject.Inject

import backend.recon.{Artist, ReconcilableFactory}
import backend.scorer.{CachedModelScorer, ModelScore, OptionalModelScore}
import backend.scorer.utils.ArtistMassScorer.Update
import genre.{Genre, GenreFinder}

import common.rich.func.MoreIteratorInstances.IteratorMonadPlus
import scalaz.Scalaz.{ToBindOpsUnapply, ToFoldableOps, ToFunctorOpsUnapply}
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
) {
  def go(update: Update): Seq[String] = {
    def goGenre(g: Genre, artists: Iterable[Artist]): OrgModeWriterMonad = {
      val filteredArtists: Iterable[(Artist, Option[ModelScore])] = for {
        artist <- artists
        score = scorer.explicitScore(artist)
        if update.filterScore(score)
      } yield (artist, score.toModelScore)
      if (filteredArtists.isEmpty) // Don't add genres without artists
        State.init[OrgModeWriter].void
      else
        OrgModeWriterMonad.append(g.name) >> filteredArtists.toVector
          .sortBy(_._1.name)
          .iterator
          .map(Function.tupled(OrgScoreFormatter.artist))
          .traverse_(OrgModeWriterMonad.append(_) |> OrgModeWriterMonad.indent)
    }

    reconcilableFactory.artistDirectories
      .groupBy(e => enumGenreFinder.apply(e.asInstanceOf[IODirectory]))
      .mapValues(_.map(reconcilableFactory.toArtist))
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
