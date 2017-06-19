package songs

import java.io.File

import backend.configs.RealConfig
import common.io.IODirectory
import common.rich.RichT._
import common.rich.func.MoreMonadPlus.SeqMonadPlus
import common.rich.path.RichFile._
import models.{IOMusicFinder, Song}

import scala.concurrent.Future
import scala.util.Random
import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToFunctorOps}

trait SongSelector {
  def randomSong: Song
  def followingSong(song: Song): Song
}

private class SongSelectorImpl(songs: IndexedSeq[File], musicFinder: IOMusicFinder) extends SongSelector
    with ToFunctorOps {
  private val random = new Random()
  def randomSong: Song = random.nextInt(songs.length) mapTo songs.apply mapTo Song.apply
  def followingSong(song: Song): Song =
    song.file.parent
        .mapTo(IODirectory.apply)
        .mapTo(musicFinder.getSongFilesInDir)
        .map(_.file)
        .fproduct(Song(_).track).map(_.swap).toMap
        .apply(song.track + 1)
        .mapTo(Song.apply)
}

object SongSelector
    extends ToBindOps with FutureInstances {

  import common.rich.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  private class SongSelectorProxy(implicit c: RealConfig) extends SongSelector {
    def update(): Future[_] = {
      val $ = Future(new SongSelectorImpl(c.mf.getSongFiles.toVector.map(_.file), c.mf))
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

  def create(implicit c: RealConfig): SongSelector = {
    // TODO TimedFuture?
    val start = System.currentTimeMillis()
    val $ = new SongSelectorProxy
    $.update().>|(c.logger.info(s"SongSelector has finished updating (${System.currentTimeMillis() - start} ms)"))
    $
  }
}
