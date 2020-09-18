package backend.albums

import backend.albums.filler.storage.FillerStorageModule
import backend.albums.filler.LocalNewAlbumsModule
import net.codingwell.scalaguice.ScalaModule

object NewAlbumModule extends ScalaModule {
  override def configure(): Unit = {
    install(FillerStorageModule)
    install(LocalNewAlbumsModule.default)
  }
}
