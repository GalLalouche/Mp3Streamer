package songs.selector

import com.google.inject.Inject
import models.Song
import musicfinder.SongDirectoryParser

import common.rich.collections.RichIterator.richIterator

private[songs] class FollowingSong @Inject() (songDirectoryParser: SongDirectoryParser) {
  def next(song: Song): Option[Song] =
    songDirectoryParser(song.file.parent).sortBy(_.trackNumber).lift(song.trackNumber)
}
