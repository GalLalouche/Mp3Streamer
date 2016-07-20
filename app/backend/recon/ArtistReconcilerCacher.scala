package backend.recon

import java.io.File

import common.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import models.{MusicFinder, Song}

import scala.concurrent.{ExecutionContext, Future}

class ArtistReconcilerCacher(repo: ReconStorage[Artist], online: OnlineReconciler[Artist])
                            (implicit ec: ExecutionContext)
    extends ReconcilerCacher[Artist](repo, online) {

  def fill(mf: MusicFinder) {
    val artists: Set[Artist] = mf.getSongFilePaths
        .map(new File(_).getParent)
        .toSet.iterator
        .map(Directory(_: String))
        .map(_.files)
        .map(_.find(e => mf.extensions.contains(e.extension)).get)
        .map(Song.apply)
        .map(_.artistName |> Artist)
        .toSet
    for (artist <- artists) {
      val recon1: Future[Option[ReconID]] =
        apply(artist).map(_._1).recover({case _ => Some("Failed to find an online match for " + artist).map(ReconID)})
      println(recon1.get)
    }
    System.exit(0)
  }
}
