package backend.search

import java.time.LocalDateTime

import models.{Album, AlbumFactory, Artist, MusicFinder, Song}
import rx.lang.scala.Observable

import scala.collection.GenSeq
import scala.concurrent.ExecutionContext

import scalaz.Semigroup
import scalaz.std.option.optionInstance
import scalaz.std.vector.vectorInstance
import scalaz.syntax.traverse.ToTraverseOps

import common.concurrency.DaemonFixedPool
import common.ds.IndexedSet
import common.io.{DirectoryRef, JsonableSaver}
import common.json.Jsonable
import common.rich.RichT._
import common.rich.RichTime._
import common.rich.primitives.RichOption._

/**
 * Caches all ID3 (in the form of JSONable models) metadata in disk. Also allows for incremental updates, in
 * case new data is added during production. Updating actions return an [[Observable]] for tracking progress.
 *
 * Although this class is the heavy lifter of the whole search indexing operation, it doesn't actually do any
 * indexing on its own! Since building the actual index from the parsed ID3 metadata is extremely fast, only
 * the ID3 metadata needs to be parsed and cached.
 */
private class MetadataCacher(
    saver: JsonableSaver,
    ec: ExecutionContext,
    mf: MusicFinder,
    albumFactory: AlbumFactory,
)(implicit
    songJsonable: Jsonable[Song],
    albumJsonable: Jsonable[Album],
    artistJsonable: Jsonable[Artist],
) {
  import MetadataCacher._

  private implicit val iec: ExecutionContext = ec

  def cacheAll(): Observable[CacheUpdate] = update(CacheAll)
  def quickRefresh(): Observable[CacheUpdate] = update(QuickRefresh)

  private object CacheAll extends Updater {
    override def targets = mf.albumDirs
    override def apply[A: Manifest : Jsonable](xs: Iterable[A]): Unit = saver saveArray xs
    override def apply(artists: IndexedSet[Artist]): Unit = apply(artists.toSeq)
  }

  // Assumed that nothing happened between the time the cache finished updating, and between the time it took
  // to save the file. PROBABLY ok :|
  private object QuickRefresh extends Updater {
    override def targets = {
      val lastUpdateTime: LocalDateTime =
        Vector(saver.lastUpdateTime[Song], saver.lastUpdateTime[Album], saver.lastUpdateTime[Artist])
            .sequenceU
            .getOrThrow(new IllegalStateException("QuickRefresh cannot be called on an empty cache"))
            .min
      mf.albumDirs.filter(_.lastModified isAfter lastUpdateTime)
    }
    override def apply[A: Manifest : Jsonable](xs: Iterable[A]): Unit = saver.update[A](_ ++ xs)
    override def apply(artists: IndexedSet[Artist]): Unit = saver.update[Artist](artists ++ _)
  }

  private val queue = DaemonFixedPool.single("MetadataCacher")
  private def update(updater: Updater): Observable[CacheUpdate] = {
    import common.concurrency.toRunnable
    val targets = updater.targets
    val totalSize = targets.length
    Observable[CacheUpdate] {obs =>
      val info = gatherInfo(targets.zipWithIndex.map {case (dir, index) =>
        ec.execute(toRunnable(() => obs onNext CacheUpdate(index + 1, totalSize, dir)))
        getDirectoryInfo(dir)
      })
      updater(info.songs)
      updater(info.albums)
      updater(info.artists)
      obs.onCompleted()
    }
        .replay
        // connect has to be invoked asynchronously, otherwise the observers would only be notified after the
        // update has been completed.
        .<|(s => queue.execute(() => s.connect))
  }

  import albumFactory.AlbumFactorySongOps

  private def getDirectoryInfo(d: DirectoryRef): DirectoryInfo =
    try {
      val songs = mf.getSongsInDir(d)
      // TODO handle empty directories
      val firstSong = songs.head
      val album = firstSong.album
      DirectoryInfo(songs, album, Artist(firstSong.artistName, Set(album)))
    }
    catch {
      case e: Throwable =>
        ec.reportFailure(e)
        throw e
    }
}

private object MetadataCacher {
  private implicit object ArtistSemigroup extends Semigroup[Artist] {
    override def append(f1: Artist, f2: => Artist): Artist = f1 merge f2
  }
  private val emptyArtistSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name)

  private case class AllInfo(songs: Vector[Song], albums: List[Album], artists: IndexedSet[Artist])
  private case class DirectoryInfo(songs: Seq[Song], album: Album, artist: Artist)
  private def gatherInfo($: GenSeq[DirectoryInfo]): AllInfo =
    $.foldLeft(AllInfo(Vector(), List(), emptyArtistSet)) {
      (agg, t) => AllInfo(agg.songs ++ t.songs, t.album :: agg.albums, agg.artists + t.artist)
    }

  case class CacheUpdate(currentIndex: Int, totalNumber: Int, dir: DirectoryRef)

  private trait Updater {
    def targets: GenSeq[DirectoryRef]
    def apply[A: Manifest : Jsonable](xs: Iterable[A]): Unit
    // Existing artists can be modified with new albums, so we can't just concatenate them as we do we the
    // other elements.
    def apply(artists: IndexedSet[Artist]): Unit
  }
}
