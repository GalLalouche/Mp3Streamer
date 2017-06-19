package backend.mb

import backend.configs.StandaloneConfig
import backend.recon.{Artist, ArtistReconStorage, ReconID, ReconcilerCacher}
import common.io.{IODirectory, IOSystem}
import common.rich.RichFuture._
import common.rich.RichT._
import models.{IOMusicFinder, MusicFinder, Song}

import scala.concurrent.{ExecutionContext, Future}

private object ArtistReconFiller {
  private implicit val config = StandaloneConfig

  private val reconciler = new ReconcilerCacher[Artist](new ArtistReconStorage(), new MbArtistReconciler())
  private def fill(mf: MusicFinder {type S = IOSystem})(implicit ec: ExecutionContext) {
    val artists: Set[Artist] = mf.getSongFiles
        .map(_.parent)
        .toSet
        .iterator
        .map((_: IODirectory).files) // why is this needed? Who knows
        .map(_.find(e => mf.extensions.contains(e.extension)).get)
        .map(_.file)
        .map(Song.apply)
        .map(_.artistName |> Artist)
        .toSet
    for (artist <- artists) {
      val recon1: Future[Option[ReconID]] =
        reconciler.apply(artist).map(_._1).recover({ case _ => Some("Failed to find an online match for " + artist).map(ReconID) })
      println(recon1.get)
    }
  }
  def main(args: Array[String]): Unit = {
    fill(new IOMusicFinder {
      override val subDirNames = List("Rock", "Metal")
    })
    System.exit(0)
  }
}
