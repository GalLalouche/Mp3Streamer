package backend.search

import common.io.JsonableSaver
import common.json.Jsonable
import javax.inject.Inject
import models.ModelJsonable._
import models.{Album, Artist, Song}

private class CompositeIndexFactory @Inject()(saver: JsonableSaver) {
  def create(): CompositeIndex = {
    val indexBuilder = WeightedIndexBuilder
    def buildIndexFromCache[T: Jsonable : WeightedIndexable : Manifest] =
      indexBuilder.buildIndexFor(saver.loadArray[T])
    new CompositeIndex(buildIndexFromCache[Song], buildIndexFromCache[Album], buildIndexFromCache[Artist])
  }
}
