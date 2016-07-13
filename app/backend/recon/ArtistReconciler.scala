package backend.recon

import java.io.File

import common.RichFuture._
import common.rich.path.Directory
import common.rich.path.RichFile._
import common.storage.OnlineRetrieverCacher
import models.{MusicFinder, Song}

import scala.concurrent.{ExecutionContext, Future}

class ArtistReconciler(repo: ReconStorage, online: OnlineReconciler)(implicit ec: ExecutionContext)
    extends OnlineRetrieverCacher[String, (Option[ID], Boolean)](repo, online(_).map(_ -> false)) {

  def fill(mf: MusicFinder) {
    val artists: Set[String] = mf.getSongFilePaths
        .map(new File(_).getParent)
        .toSet.iterator
        .map(Directory(_: String))
        .map(_.files)
        .map(_.find(e => mf.extensions.contains(e.extension)).get)
        .map(Song.apply)
        .map(_.artistName)
        .toSet
    for (artist <- artists) {
      val recon1: Future[Option[ID]] =
        get(artist).map(_._1).recover({case _ => Some("Failed to find an online match for " + artist)})
      println(recon1.get)
    }
    System.exit(0)
  }
}
