package backend.mb

import backend.module.StandaloneModule
import backend.recon.{Artist, ArtistReconStorage, ReconcilerCacher}
import backend.recon.Reconcilable._
import backend.recon.StoredReconResult.NoRecon
import com.google.inject.Guice
import models.IOMusicFinder
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import common.io.IODirectory
import common.rich.RichFuture._

private object ArtistReconFiller {
  private def fill(
      mf: IOMusicFinder,
      reconciler: ReconcilerCacher[Artist]
  )(implicit ec: ExecutionContext): Unit = mf.getSongFiles
      .map(_.parent)
      .toSet
      .map(mf.getSongsInDir(_: IODirectory).head.artist)
      .foreach {artist =>
        val recon: Future[String] = reconciler(artist).map {
          case NoRecon => s"No ReconID for <$artist>"
          case backend.recon.StoredReconResult.HasReconResult(reconId, _) => reconId.id
        }
        println(recon.get)
      }

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val reconciler = new ReconcilerCacher[Artist](
      injector.instance[ArtistReconStorage],
      injector.instance[MbArtistReconciler],
    )
    fill(new IOMusicFinder {override val subDirNames = List("Rock", "Metal")}, reconciler)
    System.exit(0)
  }
}
