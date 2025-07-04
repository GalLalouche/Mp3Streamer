package backend.module

import models.ModelsModule
import musicfinder.IOMusicFinderModule
import net.codingwell.scalaguice.ScalaModule

object IOSongsModule extends ScalaModule {
  override def configure(): Unit = {
    install(ModelsModule)
    install(IOMusicFinderModule)
  }
}
