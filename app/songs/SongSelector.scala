package songs

import backend.configs.Configuration
import common.io.RefSystem
import common.rich.RichT._
import common.rich.func.MoreMonadPlus.SeqMonadPlus
import models.{MusicFinder, Song}

import scala.concurrent.Future
import scala.util.Random
import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToFunctorOps}

trait SongSelector {
  def randomSong: Song
  def followingSong(song: Song): Song
}

private class SongSelectorImpl[Sys <: RefSystem](
    songs: IndexedSeq[Sys#F])(musicFinder: MusicFinder {type S = Sys})
    extends SongSelector with ToFunctorOps {
  private val random = new Random()
  def randomSong: Song = random.nextInt(songs.length) mapTo songs.apply mapTo musicFinder.parseSong
  def followingSong(song: Song): Song =
    song.file.parent
        .mapTo(musicFinder.getSongsInDir)
        .fproduct(_.track)
        .map(_.swap).toMap
        .apply(song.track + 1)
}

object SongSelector
    extends ToBindOps with FutureInstances {

  import common.rich.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  private class SongSelectorProxy(implicit c: Configuration) extends SongSelector {
    def update(): Future[_] = {
      val $ = Future(new SongSelectorImpl(c.mf.getSongFiles.toVector)(c.mf))
      if (songSelector == null)
        songSelector = $
      else // don't override until complete
        $.>|(songSelector = $)
      $
    }
    private var songSelector: Future[SongSelector] = _
    private lazy val ss = songSelector.get
    override def randomSong: Song = ss.randomSong
    override def followingSong(song: Song): Song = ss followingSong song
  }

  def create(implicit c: Configuration): SongSelector = {
    // TODO TimedFuture?
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy
    $.update().>|(c.logger.info(s"SongSelector has finished updating (${System.currentTimeMillis() - start} ms)"))
    $
  }
}
