package backend.configs

import backend.external.{AlbumExternalStorage, ArtistExternalStorage}
import backend.recon.{AlbumReconStorage, ArtistReconStorage}
import backend.storage.Storage
import common.rich.RichFuture._

import scala.concurrent.{ExecutionContext, Future}
import scalaz.std.{FutureInstances, ListInstances}
import scalaz.syntax.ToTraverseOps

/** Also creates all needed tables */
object CleanConfiguration extends RealConfig with NonPersistentConfig
    with FutureInstances with ListInstances with ToTraverseOps {
  override implicit val ec: ExecutionContext = ExecutionContext.global
  private def createTable(c: Storage[_, _]): Future[_] = {
    c.utils.createTable()
  }
  private def createTables() {
    implicit val c = this
    List(createTable(new ArtistReconStorage()),
      createTable(new AlbumReconStorage()),
      createTable(new ArtistExternalStorage()),
      createTable(new AlbumExternalStorage())).sequenceU.get
  }
  createTables()
}

