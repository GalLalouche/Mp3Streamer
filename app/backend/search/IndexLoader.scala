package backend.search

import jakarta.inject.Inject
import models.{AlbumDir, ArtistDir, Song}

import common.AvroableSaver
import common.io.avro.{Avroable, ModelAvroable}

private class IndexLoader @Inject() (
    saver: AvroableSaver,
    ma: ModelAvroable,
) {
  import ma._

  def loadSongs: Seq[Song] = load[Song]
  def loadAlbums: Seq[AlbumDir] = load[AlbumDir]
  def loadArtists: Seq[ArtistDir] = load[ArtistDir]

  private def load[A: Avroable: Manifest]: Seq[A] = {
    val (result, errors) = saver.loadArrayHandleErrors[A]
    errors.foreach(scribe.warn("Index parsing error; can be caused by missing/moved files", _))
    result
  }
}
