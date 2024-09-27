package backend.albums.filler

import javax.inject.Inject

import backend.recon.{Album, AlbumReconStorage, Reconciler, ReconID}

import scala.concurrent.{ExecutionContext, Future}

private class AlbumReconFiller @Inject() (
    ea: ExistingAlbums,
    reconciler: Reconciler[Album],
    storage: AlbumReconStorage,
    ec: ExecutionContext,
) {
  private val aux = new ReconFiller[Album](reconciler, storage, AlbumReconFiller.Aux)(ec)
  def go(): Future[_] = aux.go(ea.allAlbums)
}

private object AlbumReconFiller {
  private object Aux extends ReconFillerAux[Album] {
    override def musicBrainzPath = "release-group"
    override def prettyPrint(r: Album) = s"${r.artistName} - ${r.title}"
    override def verify(r: Album, id: ReconID) = Future.successful(true)
  }
}
