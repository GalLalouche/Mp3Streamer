package songs.selector

import scala.annotation.tailrec

import models.Song

trait SongSelector {
  def randomSong(): Song
  @tailrec
  private def randomSongWithExtension(ext: String): Song = {
    val $ = randomSong()
    if ($.file.extension == ext) $ else randomSongWithExtension(ext)
  }
  def randomMp3Song(): Song = randomSongWithExtension("mp3")
  def randomFlacSong(): Song = randomSongWithExtension("flac")
}
