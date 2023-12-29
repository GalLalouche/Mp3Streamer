package mains.random_folder

import java.io.File

import com.google.inject.Provides
import com.google.inject.assistedinject.FactoryModuleBuilder
import net.codingwell.scalaguice.ScalaModule

import scala.util.Random

import common.Filter

private class FolderCreatorModule(seed: Long = Random.nextLong()) extends ScalaModule {
  override def configure(): Unit = {
    bind[Long].annotatedWith[Seed].toInstance(seed)
    // TODO ScalaCommon move to somewhere common
    install(new FactoryModuleBuilder().build(classOf[RandomFolderCreatorFactory]))
  }

  private val random = new Random(seed)
  @Provides private def fileFilter(sde: SongDataExtractor): Filter[File] =
    FileFilters.fromConfig(sde)
  // TODO ScalaCommon move to somewhere common
  @Provides private def provideRandom(): Random = new Random(random.nextLong())
}
