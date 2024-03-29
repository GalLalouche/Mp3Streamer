package backend.albums

import backend.albums.filler.ExistingAlbumsModules
import backend.albums.filler.storage.FillerStorageModule
import net.codingwell.scalaguice.ScalaModule

object NewAlbumModule extends ScalaModule {
  override def configure(): Unit = {
    install(FillerStorageModule)
    install(ExistingAlbumsModules.default)
  }
}
