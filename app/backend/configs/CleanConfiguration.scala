package backend.configs

import backend.external.{AlbumExternalStorage, ArtistExternalStorage}
import backend.recon.{AlbumReconStorage, ArtistReconStorage}

import scala.concurrent.ExecutionContext

/** Also creates all needed tables */
object CleanConfiguration extends RealConfig with NonPersistentConfig {
  override implicit val ec: ExecutionContext = ExecutionContext.global
  private def createTables() {
    implicit val c = CleanConfiguration
    new ArtistReconStorage().utils.createTable()
    new AlbumReconStorage().utils.createTable()
    new ArtistExternalStorage().utils.createTable()
    new AlbumExternalStorage().utils.createTable()
  }
  createTables()
}

