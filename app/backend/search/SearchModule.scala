package backend.search

import backend.logging.Logger
import backend.search.cache.MetadataCacher
import com.google.inject.Provides
import javax.inject.Singleton
import models.{AlbumFactory, MusicFinder}
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils
import common.io.JsonableSaver

object SearchModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[JsonableSaver]
    requireBinding[MusicFinder]
    requireBinding[Logger]
  }

  @Provides @Singleton private def metadataCacher(
      saver: JsonableSaver,
      ec: ExecutionContext,
      mf: MusicFinder,
      albumFactory: AlbumFactory,
  ): MetadataCacher = {
    import models.ModelJsonable._
    new MetadataCacher(saver, ec, mf, albumFactory)
  }
}
