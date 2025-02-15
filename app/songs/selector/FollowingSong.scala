package songs.selector

import javax.inject.Inject

import models.Song
import musicfinder.MusicFinder

import common.rich.RichT.richT

private[songs] class FollowingSong @Inject() (mf: MusicFinder) {
  def next(song: Song): Option[Song] =
    song.file.parent
      .|>(mf.getSongsInDir)
      .sortBy(_.trackNumber)
      .lift(song.trackNumber)
}
