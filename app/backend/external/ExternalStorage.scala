package backend.external

import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.Freshness

import scala.concurrent.Future

import common.storage.Storage

private[external] trait ExternalStorage[R <: Reconcilable] extends Storage[R, (MarkedLinks[R], Freshness)]
private[external] trait ArtistExternalStorage extends ExternalStorage[Artist]
private[external] trait AlbumExternalStorage extends ExternalStorage[Album] {
  def deleteAllLinks(a: Artist): Future[Traversable[(String, MarkedLinks[Album], Freshness)]]
}
