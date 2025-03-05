package backend.search

import com.google.inject.Inject
import models.{AlbumDir, ArtistDir, Song}
import models.ModelJsonable.{AlbumDirJsonifier, ArtistDirJsonifier, SongJsonifier}

import common.io.JsonableSaver
import common.json.Jsonable

private class CompositeIndexFactory @Inject() (saver: JsonableSaver) {
  def create() = {
    val indexBuilder = WeightedIndexBuilder
    def loadIndex[T: Jsonable: WeightedIndexable: Manifest] = indexBuilder.buildIndexFor {
      val (result, errors) = saver.loadArrayHandleErrors[T]
      errors.foreach(scribe.warn("Index parsing error; can be caused by missing/moved files", _))
      result
    }
    new CompositeIndex(loadIndex[Song], loadIndex[AlbumDir], loadIndex[ArtistDir])
  }
}
