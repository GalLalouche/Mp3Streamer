package backend.configs

import backend.external.SlickExternalStorage
import backend.recon.{Album, AlbumReconStorage, Artist, ArtistReconStorage}

import scala.concurrent.ExecutionContext

/** Also creates all needed tables */
object CleanConfiguration extends RealConfig with NonPersistentConfig {
  override implicit val ec: ExecutionContext = ExecutionContext.global
  private def createTables() {
    implicit val c = CleanConfiguration
    new ArtistReconStorage().utils.createTable()
    new AlbumReconStorage().utils.createTable()
    new SlickExternalStorage[Artist]().utils.createTable()
    new SlickExternalStorage[Album]().utils.createTable()
  }
  createTables()
}

