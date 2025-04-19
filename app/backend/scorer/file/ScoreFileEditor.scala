package backend.scorer.file

import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.{OrgModeWriterMonad, VimLauncher}

/** Opens an .org score file for all albums and tracks for a given scoreable. */
private[scorer] class ScoreFileEditor @Inject() (
    scoreParser: ScoreParser,
    vimEditor: VimLauncher,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  /**
   * Returns a future which will complete when the file has been closed and the updated scores
   * processed.
   */
  def apply[A: Scoreable](scoreables: Seq[A]): Future[Unit] =
    vimEditor
      .withLines(OrgModeWriterMonad.run(MassScorer(scoreables)).lines, fileSuffix = ".org")
      .flatMap(scoreParser.parseLines)
}
