package backend.external

import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.Freshness

import scala.concurrent.Future

import common.storage.Storage

private trait ExternalStorage[R <: Reconcilable] extends Storage[R, (MarkedLinks[R], Freshness)]
private trait ArtistExternalStorage extends ExternalStorage[Artist]
private trait AlbumExternalStorage extends ExternalStorage[Album] {
  def deleteAllLinks(a: Artist): Future[Iterable[(String, MarkedLinks[Album], Freshness)]]
}
