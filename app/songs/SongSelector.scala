package songs

import common.io.RefSystem
import common.rich.RichT._
import javax.inject.Inject
import models.{MusicFinder, Song}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToFunctorOps}

trait SongSelector {
  def randomSong: Song
  def followingSong(song: Song): Option[Song]
}

private class SongSelectorImpl[Sys <: RefSystem](
    songs: IndexedSeq[Sys#F])(musicFinder: MusicFinder {type S = Sys})
    extends SongSelector with ToFunctorOps {
  private val random = new Random()
  def randomSong: Song = random.nextInt(songs.length) mapTo songs.apply mapTo musicFinder.parseSong
  def followingSong(song: Song): Option[Song] =
    song.file.parent
        .mapTo(musicFinder.getSongsInDir)
        .sortBy(_.track)
        .lift(song.track)
}

object SongSelector
    extends ToBindOps with FutureInstances {
  import common.rich.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  private[songs] class SongSelectorProxy @Inject()(ec: ExecutionContext, mf: MusicFinder) extends SongSelector {
    private implicit val iec: ExecutionContext = ec
    def update(): Future[_] = {
      val $ = Future(new SongSelectorImpl(mf.getSongFiles.toVector)(mf))
      if (songSelector == null)
        songSelector = $
      else // don't override until complete
        $.>|(songSelector = $)
      $
    }
    private var songSelector: Future[SongSelector] = _
    private lazy val ss = songSelector.get
    override def randomSong = ss.randomSong
    override def followingSong(song: Song) = ss followingSong song
  }
}
