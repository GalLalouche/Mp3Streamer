package backend.scorer.utils

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.{OrgModeWriterMonad, VimLauncher}

class ScoreFileEditor @Inject() (
    scoreParser: ScoreParser,
    vimEditor: VimLauncher,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply[A: Scoreable](scoreables: Seq[A]): Future[Unit] =
    vimEditor
      .withLines(OrgModeWriterMonad.run(MassScorer(scoreables)).lines, fileSuffix = ".org")
      .flatMap(scoreParser.parseLines)
}
