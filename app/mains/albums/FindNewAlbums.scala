package mains.albums

import backend.mb.MbArtistReconciler
import backend.recon.{ArtistReconStorage, ReconcilerCacher}
import common.rich.RichT._
import common.rich.path.Directory
import models.RealLocations

import scala.concurrent.ExecutionContext.Implicits.global

object FindNewAlbums {
  def main(args: Array[String]) {
    val $ = new NewAlbumsRetriever(new ReconcilerCacher(new ArtistReconStorage(), new MbArtistReconciler()), new RealLocations {
      override val subDirs: List[String] = List("Rock", "Metal")
    })
    val file = Directory("C:/").addFile("albums.txt").clear()
    $.findNewAlbums.map(_.toString.log()).toSeq.sorted.foreach(file appendLine)
    println("Done")
    System.exit(0)
  }
}
