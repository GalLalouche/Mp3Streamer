package songs

import java.io.File

import common.concurrency.DirectoryWatcher
import common.concurrency.DirectoryWatcher.DirectoryEvent
import common.io.IODirectory
import common.rich.RichT._
import common.rich.func.MoreMonadPlus._
import common.rich.path.RichFile._
import controllers.Searcher
import models.{MusicFinder, Song}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import rx.lang.scala.Subscriber
import search.MetadataCacher

import scala.concurrent.Future
import scala.util.Random
import scalaz.Scalaz._

trait SongSelector {
  def randomSong: Song
  def followingSong(song: Song): Song
}

private class SongSelectorImpl(songs: IndexedSeq[File], musicFinder: MusicFinder) extends SongSelector {
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

object SongSelector {

  import common.RichFuture._

  /** A mutable-updateable wrapper of SongSelector */
  private class SongSelectorProxy(musicFinder: MusicFinder) extends SongSelector {
    def update(): Future[Unit] = {
      val $ = Future(new SongSelectorImpl(musicFinder.getSongFilePaths.toVector.map(new File(_)), musicFinder))
      if (songSelector == null)
        songSelector = $
      else // don't override
        $.onSuccess {case e => songSelector = $}
      $.map(e => ())
    }
    private var songSelector: Future[SongSelector] = null
    private lazy val ss = songSelector.get
    override def randomSong: Song = ss.randomSong
    override def followingSong(song: Song): Song = ss followingSong song
  }
  
  def listen(musicFinder: MusicFinder): SongSelector = {
    val $ = new SongSelectorProxy(musicFinder)
    def directoryListener(e: DirectoryEvent): Unit = e match {
      case DirectoryWatcher.DirectoryCreated(d) =>
        Logger info s"Directory $d has been added"
        $.update()
            .onEnd(MetadataCacher ! new IODirectory(d))
            .onEnd(Searcher.!())
      case DirectoryWatcher.DirectoryDeleted(d) =>
        Logger warn s"Directory has been deleted; the index does not support deletions yet, so please update."
        $.update()
      case DirectoryWatcher.Started => common.CompositeLogger info "SongSelector is listening to directory changes"
      case _ => ()
    }
    DirectoryWatcher.apply(musicFinder).subscribe(Subscriber(directoryListener))
    $.update()
    $
  }
}
