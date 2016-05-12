package mains.albums

import models.{MusicFinder, Song}

import scala.concurrent.{ExecutionContext, Future}
import common.rich.path.RichFile._
import common.RichFuture._
import java.io.File

import common.rich.path.Directory
import common.storage.OnlineRetrieverCacher

class Reconciler(repo: ReconStorage, online: OnlineReconciler)(implicit ec: ExecutionContext)
    extends OnlineRetrieverCacher[String, Option[ID]](repo, online) {
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
      val recon1: Future[Option[ID]] = get(artist).recover({case _ => Some("Failed to find an online match for " + artist)})
      println(recon1.get)
    }
    System.exit(0)
  }
}
