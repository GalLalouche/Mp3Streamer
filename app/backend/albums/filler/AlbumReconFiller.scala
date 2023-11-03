package backend.albums.filler

import backend.logging.Logger
import backend.recon.{Album, AlbumReconStorage, Reconciler, ReconID}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

private class AlbumReconFiller @Inject() (
    ea: ExistingAlbums,
    reconciler: Reconciler[Album],
    storage: AlbumReconStorage,
    ec: ExecutionContext,
    logger: Logger,
) {
  private val aux = new ReconFiller[Album](reconciler, storage, AlbumReconFiller.Aux, logger)(ec)
  def go(): Future[_] = aux.go(ea.allAlbums)
}

private object AlbumReconFiller {
  private object Aux extends ReconFillerAux[Album] {
    override def musicBrainzPath = "release-group"
    override def prettyPrint(r: Album) = s"${r.artistName} - ${r.title}"
    override def verify(r: Album, id: ReconID) = Future.successful(true)
  }
}
