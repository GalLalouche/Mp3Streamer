package songs.selector

import com.google.inject.Inject
import models.Song
import musicfinder.SongDirectoryParser

import common.rich.RichT.richT
import common.rich.collections.RichSeqView.richSeqView

private[songs] class FollowingSong @Inject() (songDirectoryParser: SongDirectoryParser) {
  def next(song: Song): Option[Song] =
    song.file.parent.|>(songDirectoryParser.apply).castSortBy(_.trackNumber).lift(song.trackNumber)
}
