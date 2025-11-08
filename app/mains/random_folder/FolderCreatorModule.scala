package mains.random_folder

import java.io.File

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.util.Random

import common.Filter
import common.guice.ModuleUtils

private class FolderCreatorModule(seed: Long = Random.nextLong())
    extends ScalaModule
    with ModuleUtils {
  private val random = new Random(seed)

  override def configure(): Unit = {
    bind[Long].annotatedWith[Seed].toInstance(seed)
    install[RandomFolderCreatorFactory]
    bind[Random].toInstance(random)
  }

  @Provides private def fileFilter(sde: SongDataExtractor): Filter[File] =
    FileFilters.fromConfig(sde)
}
