package backend.scorer.utils

import backend.recon.{Artist, ReconcilableFactory}
import backend.scorer.{CachedModelScorer, ModelScore}
import backend.scorer.utils.ArtistMassScorer.Update
import javax.inject.Inject
import models.{Genre, GenreFinder}

import scala.concurrent.ExecutionContext

import scalaz.std.vector.vectorInstance
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.Scalaz.{ToBindOpsUnapply, ToFoldableOps, ToFunctorOpsUnapply}
import scalaz.State
import common.rich.func.MoreIteratorInstances.IteratorMonadPlus

import common.{OrgModeWriter, OrgModeWriterMonad}
import common.io.IODirectory
import common.rich.RichT._
import common.OrgModeWriterMonad.OrgModeWriterMonad

/**
* Creates an .org file for faster updating of artists.
* See [[ScoreParser]] for the parser of the output file.
*/
// REMAINING Support for albums, songs
private class ArtistMassScorer @Inject()(
    scorer: CachedModelScorer,
    reconcilableFactory: ReconcilableFactory,
    enumGenreFinder: GenreFinder,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  def go(update: Update): Seq[String] = {
    def goGenre(g: Genre, artists: Iterable[Artist]): OrgModeWriterMonad = {
      val filteredArtists: Iterable[(Artist, Option[ModelScore])] = for {
        artist <- artists
        score = scorer(artist)
        if update.filterScore(score)
      } yield (artist, score)
      if (filteredArtists.isEmpty) // Don't add genres without artists
        State.init[OrgModeWriter].void
      else
        OrgModeWriterMonad.append(g.name) >> filteredArtists
            .toVector
            .sortBy(_._1.name)
            .iterator
            .map(Function.tupled(OrgScoreFormatter.artist))
            .traverse_(OrgModeWriterMonad.append(_) |> OrgModeWriterMonad.indent)
    }

    reconcilableFactory.artistDirectories
        .groupBy(enumGenreFinder apply _.asInstanceOf[IODirectory])
        .mapValues(_.map(reconcilableFactory dirNameToArtist _.name))
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
    def filterScore(s: Option[ModelScore]): Boolean = this match {
      case Update.NoScore => s.isEmpty
      case Update.All => true
    }
  }
  object Update {
    case object NoScore extends Update
    case object All extends Update
  }
}
