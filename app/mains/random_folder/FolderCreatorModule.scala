package mains.random_folder

import backend.module.StandaloneModule
import com.google.inject.Provides
import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

private object FolderCreatorModule extends ScalaModule {
  override def configure(): Unit = {
    install(StandaloneModule)
    // TODO add to scala guice.
    install(new FactoryModuleBuilder()
        .implement(classOf[RandomFolderCreator], classOf[RandomFolderCreator])
        .build(classOf[RandomFolderCreatorFactory])
    )
  }
  @Provides private def fileFilter(sde: SongDataExtractor): FileFilter =
    FileFilter.fromConfig(sde)
}
