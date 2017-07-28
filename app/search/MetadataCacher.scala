package search

import java.time.{LocalDateTime, ZoneOffset}

import backend.configs.Configuration
import common.Jsonable
import common.concurrency.SimpleTypedActor
import common.ds.{Collectable, IndexedSet}
import common.io._
import models._
import rx.lang.scala.Observable
import rx.lang.scala.subjects.ReplaySubject

import scala.collection.GenSeq
import scala.concurrent.Future
import scalaz.Semigroup
import scalaz.std.{AnyValInstances, FutureInstances, ListInstances, OptionInstances}
import scalaz.syntax.{ToBindOps, ToTraverseOps}

class MetadataCacher(saver: JsonableSaver)(implicit val c: Configuration,
                                           songJsonable: Jsonable[Song],
                                           albumJsonable: Jsonable[Album],
                                           artistJsonable: Jsonable[Artist])
    extends OptionInstances with ListInstances with AnyValInstances
        with ToTraverseOps with FutureInstances with ToBindOps {
  import MetadataCacher._
  private sealed trait UpdateType {
    def apply(): Observable[IndexUpdate]
  }
  private object UpdateAll extends UpdateType {
    override def apply(): Observable[IndexUpdate] =
      updateIndex(c.mf.albumDirs, new AllInfoHandler {
        override def apply[T: Manifest : Jsonable](xs: Seq[T]): Unit = saver save xs
        override def apply(artists: IndexedSet[Artist]): Unit = apply(artists.toSeq)
      })
  }
  private object QuickRefresh extends UpdateType {
    override def apply(): Observable[IndexUpdate] = {
      val lastUpdateTime: Option[LocalDateTime] =
        List(saver.lastUpdateTime[Song], saver.lastUpdateTime[Album], saver.lastUpdateTime[Artist])
            .sequence[Option, LocalDateTime]
            .flatMap(_.minimumBy(_.toEpochSecond(ZoneOffset.UTC)))
      lastUpdateTime.fold(indexAll()) { lastUpdateTime =>
        updateIndex(c.mf.albumDirs.filter(_.lastModified isAfter lastUpdateTime), new AllInfoHandler {
          override def apply[T: Manifest : Jsonable](xs: Seq[T]): Unit = saver.update[T](_ ++ xs)
          override def apply(artists: IndexedSet[Artist]): Unit = saver.update[Artist](artists ++ _)
        })
      }
    }
  }
  private case class UpdateDir(dir: DirectoryRef) extends UpdateType {
    override def apply(): Observable[IndexUpdate] =
      Observable.from(Future {
        val info = getDirectoryInfo(dir, () => ())
        saver.update[Song](_ ++ info.songs)
        saver.update[Album](_.toSet + info.album)
        saver.update[Artist](_./:(emptyArtistSet)(_ + _) + info.artist)
        IndexUpdate(1, 1, dir)
      })
  }

  // TODO handle empty directories
  private def getDirectoryInfo(d: DirectoryRef, onParsingCompleted: () => Unit): DirectoryInfo = {
    val songs = c.mf getSongsInDir d
    val album = songs.head.album
    onParsingCompleted()
    DirectoryInfo(songs, album, Artist(songs.head.artistName, Set(album)))
  }
  private val updatingActor = new SimpleTypedActor[UpdateType, Observable[IndexUpdate]] {
    override def apply(u: UpdateType): Observable[IndexUpdate] = u.apply()
  }

  private def update(u: UpdateType): Observable[IndexUpdate] = Observable.from(updatingActor ! u).flatten

  def processDirectory(dir: DirectoryRef): Future[Unit] = {
    import common.rich.RichObservable._
    update(UpdateDir(dir)).toFuture.>|(Unit)
  }

  private def updateIndex(dirs: GenSeq[DirectoryRef], allInfoHandler: AllInfoHandler): Observable[IndexUpdate] = {
    val $ = ReplaySubject[IndexUpdate]
    Observable[IndexUpdate](obs => {
      val totalSize = dirs.length
      Future {
        import common.concurrency.toRunnable
        gatherInfo(dirs.zipWithIndex.map { case (d, j) =>
          getDirectoryInfo(d, onParsingCompleted = () => {
            c execute (() => obs onNext IndexUpdate(j + 1, totalSize, d))
          })
        })
      } map { info =>
        allInfoHandler(info.songs)
        allInfoHandler(info.albums)
        allInfoHandler(info.artists)
      }
    }.>|(obs.onCompleted())) subscribe $
    $
  }

  def indexAll(): Observable[IndexUpdate] = update(UpdateAll)

  // There is a hidden assumption here, that nothing happened between the time the index finished updating,
  // and between the time it took to save the file. PROBABLY ok :|
  def quickRefresh(): Observable[IndexUpdate] = update(QuickRefresh)
}

/**
  * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
  * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
  * production.
  */
object MetadataCacher {
  private implicit object ArtistSemigroup extends Semigroup[Artist] {
    override def append(f1: Artist, f2: => Artist): Artist = f1 merge f2
  }
  private val emptyArtistSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name)
  private case class AllInfo(songs: Seq[Song], albums: List[Album], artists: IndexedSet[Artist])
  private case class DirectoryInfo(songs: Seq[Song], album: Album, artist: Artist)
  private implicit object AllInfoCollectable extends Collectable[DirectoryInfo, AllInfo] {
    override def empty: AllInfo = AllInfo(List(), List(), emptyArtistSet)
    override def +(agg: AllInfo, t: DirectoryInfo): AllInfo =
      AllInfo(t.songs ++ agg.songs, t.album :: agg.albums, agg.artists + t.artist)
  }
  private def gatherInfo($: GenSeq[DirectoryInfo]) = Collectable fromList $
  case class IndexUpdate(currentIndex: Int, totalNumber: Int, dir: DirectoryRef)
  // Polymorphic functions? Hmmm...
  private trait AllInfoHandler {
    def apply[T: Manifest : Jsonable](xs: Seq[T]): Unit
    def apply(artists: IndexedSet[Artist]): Unit
  }

  def create(implicit c: Configuration,
             songJsonable: Jsonable[Song],
             albumJsonable: Jsonable[Album],
             artistJsonable: Jsonable[Artist]): MetadataCacher = {
    import c._
    new MetadataCacher(new JsonableSaver)
  }
}
