package backend.lyrics.retrievers

import backend.recon.Artist
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.storage.{SetStorageImpl, TableUtils}

private class InstrumentalArtistStorageImpl @Inject() (
    slickInstrumentalArtistStorage: SlickInstrumentalArtistStorage,
    ec: ExecutionContext,
) extends InstrumentalArtistStorage {
  private val impl = new SetStorageImpl[Artist](slickInstrumentalArtistStorage)(ec)
  override def add(a: Artist): Future[Unit] = impl.add(a)
  override def delete(a: Artist): Future[Boolean] = impl.delete(a)
  override def contains(a: Artist): Future[Boolean] = impl.contains(a)
  override def utils: TableUtils = impl.utils
}
