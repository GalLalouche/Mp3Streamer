package backend.scorer.utils

import backend.scorer.utils.Scoreable.ScoreableOps

import common.rich.func.MoreTraverseInstances._
import scalaz.Scalaz.{ToBindOps, ToFoldableOps, ToFunctorOpsUnapply}
import scalaz.State

import common.{OrgModeWriter, OrgModeWriterMonad}
import common.OrgModeWriterMonad.OrgModeWriterMonad
import common.rich.RichT.richT

/**
 * Creates an .org file for faster updating of artists. See [[ScoreParser]] for the parser of the
 * output file.
 */
// REMAINING Support for albums, songs
private object MassScorer {
  def apply[A: Scoreable](scoreables: Seq[A]): OrgModeWriterMonad = scoreables.traverse_(go(_))
  private def go[A](scoreable: A)(implicit ev: Scoreable[A]): OrgModeWriterMonad =
    OrgModeWriterMonad.append(scoreable.scoreFormat) >>
      (if (scoreable.isEmpty)
         State.init[OrgModeWriter].void
       else
         ev.children(scoreable).traverse_(go(_)(ev.childrenEv) |> OrgModeWriterMonad.indent))
}
