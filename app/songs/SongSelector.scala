package songs

import java.io.File

import common.io.IODirectory
import common.rich.RichT._
import common.rich.collections.RichTraversable._
import common.rich.path.RichFile._
import models.{MusicFinder, Song}
import rx.lang.scala.Observable

import scala.util.Random

class SongSelector private(songs: IndexedSeq[File], musicFinder: MusicFinder) {
  private val random = new Random()
  def randomSong: Song = songs(random nextInt songs.length) |> Song.apply
  def followingSong(song: Song): Song = {
    song.file.parent
        .mapTo(IODirectory.apply)
        .mapTo(musicFinder.getSongFilePathsInDir)
        .map(new File(_))
        .zipMap(Song(_).track).map(_.swap).toMap
        .apply(song.track + 1)
        .mapTo(Song.apply)
  }
}

object SongSelector {
  def from(musicFinder: MusicFinder): SongSelector =
    new SongSelector(musicFinder.getSongFilePaths.toVector.map(new File(_)), musicFinder)
  def listen(musicFinder: MusicFinder, observable: Observable[Any]): Observable[SongSelector] =
    observable.map(_ => from(musicFinder))
}
