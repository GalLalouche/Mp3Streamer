package backend.new_albums

import backend.new_albums.filler.ExistingAlbumsModules
import backend.new_albums.filler.storage.FillerStorageModule
import net.codingwell.scalaguice.ScalaModule

object NewAlbumsModule extends ScalaModule {
  override def configure(): Unit = {
    install(FillerStorageModule)
    install(ExistingAlbumsModules.default)
  }
}
