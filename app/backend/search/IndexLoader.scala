package backend.search

import jakarta.inject.Inject
import models.{AlbumDir, ArtistDir, ModelJsonable, Song}

import common.json.Jsonable
import common.json.saver.JsonableSaver

private class IndexLoader @Inject() (
    saver: JsonableSaver,
    mj: ModelJsonable,
) {
  import mj._

  def loadSongs: Seq[Song] = load[Song]
  def loadAlbums: Seq[AlbumDir] = load[AlbumDir]
  def loadArtists: Seq[ArtistDir] = load[ArtistDir]

  private def load[A: Jsonable: Manifest]: Seq[A] = {
    val (result, errors) = saver.loadArrayHandleErrors[A]
    errors.foreach(scribe.warn("Index parsing error; can be caused by missing/moved files", _))
    result
  }
}
