package songs

import java.io.File

import backend.configs.Configuration
import common.concurrency.DirectoryWatcher
import common.concurrency.DirectoryWatcher.DirectoryEvent
import common.io.IODirectory
import common.rich.RichT._
import common.rich.func.MoreMonadPlus.SeqMonadPlus
import common.rich.path.RichFile._
import controllers.{Cacher, Searcher}
import models.{MusicFinder, Song}
import rx.lang.scala.Subscriber

import scala.concurrent.Future
import scala.util.Random
import scalaz.std.FutureInstances
import scalaz.syntax.{ToBindOps, ToFunctorOps}

trait SongSelector {
  def randomSong: Song
  def followingSong(song: Song): Song
}

private class SongSelectorImpl(songs: IndexedSeq[File], musicFinder: MusicFinder) extends SongSelector
    with ToFunctorOps {
  private val random = new Random()
  def randomSong: Song = random.nextInt(songs.length) mapTo songs.apply mapTo Song.apply
  def followingSong(song: Song): Song =
    song.file.parent
        .mapTo(IODirectory.apply)
        .mapTo(musicFinder.getSongFilePathsInDir)
        .map(new File(_))
        .fproduct(Song(_).track).map(_.swap).toMap
        .apply(song.track + 1)
        .mapTo(Song.apply)
}

object SongSelector
    extends ToBindOps with FutureInstances {

  import common.rich.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  private class SongSelectorProxy(implicit c: Configuration) extends SongSelector {
    def update(): Future[_] = {
      val $ = Future(new SongSelectorImpl(c.mf.getSongFilePaths.toVector.map(new File(_)), c.mf))
      if (songSelector == null)
        songSelector = $
      else // don't override
        $.onSuccess { case e => songSelector = $ }
      $
    }
    private var songSelector: Future[SongSelector] = _
    private lazy val ss = songSelector.get
    override def randomSong: Song = ss.randomSong
    override def followingSong(song: Song): Song = ss followingSong song
  }

  def listen(implicit c: Configuration): SongSelector = {
    val logger = c.logger
    val $ = new SongSelectorProxy
    def directoryListener(e: DirectoryEvent): Unit = e match {
      case DirectoryWatcher.DirectoryCreated(d) =>
        logger info s"Directory $d has been added"
        $.update() >> (Cacher newDir new IODirectory(d)) >> Searcher.!()
      case DirectoryWatcher.DirectoryDeleted(d) =>
        logger warn "Directory has been deleted; the index does not support deletions yet, so please update."
        $.update()
      case DirectoryWatcher.Started => logger info "SongSelector is listening to directory changes"
      case _ => ()
    }
    DirectoryWatcher.apply(c.mf).subscribe(Subscriber(directoryListener))
    $.update()
    $
  }
}
