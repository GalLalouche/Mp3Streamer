package backend.score.file

import backend.score.file.Scoreable.{ScoreableImpl, ScoreableOps}
import com.google.inject.Inject
import models.{ArtistDir, ArtistDirFactory, Song}

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.MoreTraverseInstances._
import scalaz.Scalaz.{ToBindOps, ToFoldableOps, ToFunctorOpsUnapply}
import scalaz.State

import common.{OrgModeWriter, OrgModeWriterMonad, VimLauncher}
import common.OrgModeWriterMonad.OrgModeWriterMonad
import common.rich.RichT.richT

private[score] class FileScorer @Inject() (
    ec: ExecutionContext,
    artistFactory: ArtistDirFactory,
    scoreableImpl: ScoreableImpl,
    scoreParser: ScoreParser,
    vimEditor: VimLauncher,
) {
  private implicit val iec: ExecutionContext = ec
  private implicit val artistScoreable: Scoreable[ArtistDir] = scoreableImpl.artistDir

  def apply(song: Song): Future[Unit] = {
    val lines = OrgModeWriterMonad.run(go(artistFactory.fromSong(song))).lines
    vimEditor.withLines(lines, fileSuffix = ".org").flatMap(scoreParser.parseLines)
  }

  private def go[A](scoreable: A)(implicit ev: Scoreable[A]): OrgModeWriterMonad =
    OrgModeWriterMonad
      .append(scoreable.scoreFormat)
      .>>(
        if (scoreable.isEmpty)
          State.init[OrgModeWriter].void
        else
          ev.children(scoreable).traverse_(go(_)(ev.childrenEv) |> OrgModeWriterMonad.indent),
      )
}
