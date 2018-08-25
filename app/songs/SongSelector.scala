package songs

import backend.configs.Configuration
import backend.logging.Logger
import common.io.RefSystem
import common.rich.RichT._
import models.{MusicFinder, Song}
import net.codingwell.scalaguice.InjectorExtensions._

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
  private class SongSelectorProxy(implicit c: Configuration) extends SongSelector {
    private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    private val mf = c.injector.instance[MusicFinder]
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

  def create(implicit c: Configuration): SongSelector = {
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    val logger = c.injector.instance[Logger]
    // TODO TimedFuture?
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy
    $.update().>|(logger.info(s"SongSelector has finished updating (${System.currentTimeMillis() - start} ms)"))
    $
  }
}
