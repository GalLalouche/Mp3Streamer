package backend.search

import javax.inject.Inject

import models.{Album, Artist, Song}
import models.ModelJsonable.{AlbumJsonifier, ArtistJsonifier, SongJsonifier}

import common.io.JsonableSaver
import common.json.Jsonable

private class CompositeIndexFactory @Inject() (saver: JsonableSaver) {
  def create() = {
    val indexBuilder = WeightedIndexBuilder
    def loadIndex[T: Jsonable: WeightedIndexable: Manifest] =
      indexBuilder.buildIndexFor(saver.loadArray[T])
    new CompositeIndex(loadIndex[Song], loadIndex[Album], loadIndex[Artist])
  }
}
