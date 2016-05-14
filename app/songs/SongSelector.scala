package songs

import java.io.File

import common.rich.RichT._
import models.{MusicFinder, Song}
import rx.lang.scala.Observable

import scala.util.Random

class SongSelector private(songs: IndexedSeq[File]) {
  private val random = new Random()
  def randomSong: Song = songs(random nextInt songs.length) |> Song.apply
}

object SongSelector {
  def from(musicFinder: MusicFinder): SongSelector =
    new SongSelector(musicFinder.getSongFilePaths.toVector.map(new File(_)))
  def listen(musicFinder: MusicFinder, observable: Observable[Any]): Observable[SongSelector] =
    observable.map(_ => from(musicFinder))
}
