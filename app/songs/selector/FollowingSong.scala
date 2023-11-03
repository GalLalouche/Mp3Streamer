package songs.selector

import javax.inject.Inject

import common.rich.RichT.richT
import models.{MusicFinder, Song}

private[songs] class FollowingSong @Inject() (mf: MusicFinder) {
  def next(song: Song): Option[Song] =
    song.file.parent
      .|>(mf.getSongsInDir)
      .sortBy(_.track)
      .lift(song.track)
}
