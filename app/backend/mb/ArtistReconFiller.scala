package backend.mb

import java.io.File

import backend.recon.{Artist, ArtistReconStorage, ReconID, ReconcilerCacher}
import common.RichFuture._
import common.rich.RichT._
import common.rich.path.Directory
import common.rich.path.RichFile._
import models.{MusicFinder, RealLocations, Song}

import scala.concurrent.{ExecutionContext, Future}

object ArtistReconFiller {
  import scala.concurrent.ExecutionContext.Implicits.global
  
  val reconciler = new ReconcilerCacher[Artist](new ArtistReconStorage(), new MbArtistReconciler())
  def fill(mf: MusicFinder)(implicit ec: ExecutionContext) {
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
        reconciler.apply(artist).map(_._1).recover({case _ => Some("Failed to find an online match for " + artist).map(ReconID)})
      println(recon1.get)
    }
  }
  def main(args: Array[String]): Unit = {
    fill(new RealLocations {override val subDirs = List("Rock", "Metal")})
    System.exit(0)
  }
}
