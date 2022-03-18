package backend.scorer

import backend.recon.{Artist, ReconcilableFactory}
import backend.scorer.ArtistMassScorer._
import javax.inject.Inject
import models.{Genre, GenreFinder}

import scala.concurrent.ExecutionContext

import scalaz.std.vector.vectorInstance
import scalaz.syntax.traverse.ToTraverseOps
import scalaz.Scalaz.{ToBindOpsUnapply, ToFunctorOpsUnapply}
import scalaz.State

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
    def goArtist(artist: Artist): OrgModeWriterMonad = {
      val score = scorer(artist) getOrElse ModelScore.Default
      if (update filterScore score) OrgModeWriterMonad.append(OrgScoreFormatter.artist(artist, score))
      // TODO ScalaCommon Point.void
      else State.init[OrgModeWriter].void
    }

    def goGenre(g: Genre, artists: Iterable[Artist]): OrgModeWriterMonad =
      OrgModeWriterMonad.append(g.name) >> artists
          .toVector
          .sortBy(_.name)
          // TODO ScalaCommon traverse_
          .traverse(goArtist(_) |> OrgModeWriterMonad.indent)
          .void

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
    def filterScore(s: ModelScore): Boolean = this match {
      case Update.DefaultOnly => s == ModelScore.Default
      case Update.All => true
    }
  }
  object Update {
    case object DefaultOnly extends Update
    case object All extends Update
  }

  private def scoreString(depth: Int, string: String): String =
    s"${"*" * depth} $string"
}
