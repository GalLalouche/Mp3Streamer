package backend.mb

import backend.module.StandaloneModule
import backend.recon.{Artist, ArtistReconStorage, ReconcilerCacher, ReconID}
import com.google.inject.Guice
import common.io.{IODirectory, IOSystem}
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.func.ToMoreMonadErrorOps
import models.{IOMusicFinder, MusicFinder, Song}
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

private object ArtistReconFiller
    extends ToMoreMonadErrorOps with FutureInstances {
  val injector = Guice createInjector StandaloneModule
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]

  private val reconciler = new ReconcilerCacher[Artist](
    injector.instance[ArtistReconStorage],
    injector.instance[MbArtistReconciler],
  )
  private def fill(mf: MusicFinder {type S = IOSystem})(implicit ec: ExecutionContext): Unit = {
    val artists: Set[Artist] = mf.getSongFiles
        .map(_.parent)
        .toSet
        .iterator
        .map((_: IODirectory).files) // why is this needed? Who knows
        .map(_.find(e => mf.extensions.contains(e.extension)).get)
        .map(_.file)
        .map(Song.apply)
        .map(_.artistName |> Artist.apply)
        .toSet
    for (artist <- artists) {
      val recon: Future[Option[ReconID]] =
        reconciler.apply(artist).map(_._1) orElse Some(ReconID("Failed to find an online match for " + artist))
      println(recon.get)
    }
  }
  def main(args: Array[String]): Unit = {
    fill(new IOMusicFinder {
      override val subDirNames = List("Rock", "Metal")
    })
    System.exit(0)
  }
}
