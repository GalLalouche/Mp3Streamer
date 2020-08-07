package mains.random_folder

import backend.module.StandaloneModule
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

private object FolderCreatorModule extends ScalaModule {
  override def configure(): Unit = {
    install(StandaloneModule)
  }
  @Provides private def fileFilter(sde: SongDataExtractor): FileFilter =
    FileFilter.fromConfig(sde)
}
