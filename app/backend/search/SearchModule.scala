package backend.search

import backend.logging.Logger
import com.google.inject.Provides
import common.ModuleUtils
import common.io.JsonableSaver
import common.json.Jsonable
import models.{Album, Artist, MusicFinder, Song}
import net.codingwell.scalaguice.ScalaModule

object SearchModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    requireBinding[JsonableSaver]
    requireBinding[MusicFinder]
    requireBinding[Logger]
  }

  @Provides private def provideCompositeIndex(saver: JsonableSaver): CompositeIndex = {
    import models.ModelJsonable._
    val indexBuilder = WeightedIndexBuilder
    def buildIndexFromCache[T: Jsonable : WeightedIndexable : Manifest] =
      indexBuilder.buildIndexFor(saver.loadArray[T])
    new CompositeIndex(buildIndexFromCache[Song], buildIndexFromCache[Album], buildIndexFromCache[Artist])
  }
}
