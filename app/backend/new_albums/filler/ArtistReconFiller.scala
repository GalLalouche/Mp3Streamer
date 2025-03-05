package backend.new_albums.filler

import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ArtistReconStorage, ReconID}
import com.google.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions._

import scala.concurrent.{ExecutionContext, Future}

import common.rich.RichFuture._

private class ArtistReconFiller @Inject() (
    ea: ExistingAlbums,
    reconciler: MbArtistReconciler,
    storage: ArtistReconStorage,
    verifier: ArtistReconVerifier,
    ec: ExecutionContext,
) {
  private object Aux extends ReconFillerAux[Artist] {
    override def musicBrainzPath = "artist"
    override def prettyPrint(r: Artist) = r.name
    override def verify(r: Artist, id: ReconID) = verifier(r, id)
  }

  private val aux = new ReconFiller[Artist](reconciler, storage, Aux)(ec)

  def go(): Future[_] = aux.go(ea.artists)
}

private object ArtistReconFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.lazyAlbums)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[ArtistReconFiller].go().get
  }
}
