package search

import java.io.File

import backend.configs.Configuration
import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.{Collectable, IndexedSet}
import models._
import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.{Observable, Observer}

import scala.collection.GenSeq

// Possible source of bugs: indexAll and apply(DirectoryRef) work on different threads. This solution is forced
// due to type erasure.
class MetadataCacher(saver: JsonableSaver)(implicit c: Configuration) extends SimpleActor[DirectoryRef] {

  import MetadataCacher._

  private val mf = c.mf

  private def getDirectoryInfo(d: DirectoryRef, onParsingCompleted: () => Unit): DirectoryInfo = {
    val songs = mf.getSongFilePathsInDir(d).map(parseSong)
    val album = songs.head.album
    onParsingCompleted()
    DirectoryInfo(songs, album, Artist(songs.head.artistName, Set(album)))
  }
  protected def parseSong(filePath: String): Song = Song(new File(filePath))
  override def apply(dir: DirectoryRef) {
    val info = getDirectoryInfo(dir, () => ())
    saver.update[Song](_ ++ info.songs)
    saver.update[Album](_.toSet + info.album)
    saver.update[Artist](_./:(emptyArtistSet)(_ + _) + info.artist)
  }
  def indexAll(): Observable[IndexUpdate] = {
    import common.concurrency.toRunnable
    def aux(obs: Observer[IndexUpdate]) {
      c execute (() => {
        val dirs: GenSeq[DirectoryRef] = mf.albumDirs
        val totalSize = dirs.length
        val $ = gatherInfo(dirs.zipWithIndex.map { case (d, j) => getDirectoryInfo(d, onParsingCompleted = () => {
           c execute (() => obs onNext IndexUpdate(j + 1, totalSize, d))
        })
        })
        saver.save($.songs)
        saver.save($.albums)
        saver.save($.artists)
        obs.onCompleted()
      })
    }
    val $ = ReplaySubject[IndexUpdate]
    Observable(aux) subscribe $
    $
  }
}

/**
 * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
 * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
 * production.
 */
object MetadataCacher {
  private val emptyArtistSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name, _ merge _)
  private case class AllInfo(songs: Seq[Song], albums: List[Album], artists: IndexedSet[Artist])
  private case class DirectoryInfo(songs: Seq[Song], album: Album, artist: Artist)
  private implicit object AllInfoCollectable extends Collectable[DirectoryInfo, AllInfo] {
    override def empty: AllInfo = AllInfo(List(), List(), emptyArtistSet)
    override def +(agg: AllInfo, t: DirectoryInfo): AllInfo =
      AllInfo(t.songs ++ agg.songs, t.album :: agg.albums, agg.artists + t.artist)
  }
  private def gatherInfo($: GenSeq[DirectoryInfo]) = Collectable fromList $
  case class IndexUpdate(currentIndex: Int, totalNumber: Int, dir: DirectoryRef)

  def create(implicit c: Configuration): MetadataCacher = {
    import c._
    new MetadataCacher(new JsonableSaver)
  }
}
