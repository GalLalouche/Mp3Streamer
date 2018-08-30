package backend.search

import common.io.JsonableSaver
import common.json.Jsonable
import javax.inject.Inject
import models.{Album, AlbumFactory, Artist, MusicFinder, Song}

import scala.concurrent.ExecutionContext

private class MetadataCacherFactory @Inject()(
    saver: JsonableSaver,
    ec: ExecutionContext,
    mf: MusicFinder,
    albumFactory: AlbumFactory,
) {
  def create(implicit
      songJsonable: Jsonable[Song],
      albumJsonable: Jsonable[Album],
      artistJsonable: Jsonable[Artist],
  ): MetadataCacher = new MetadataCacher(saver, ec, mf, albumFactory)
}
