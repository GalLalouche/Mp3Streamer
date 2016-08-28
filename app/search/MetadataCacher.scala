package search

import java.io.File
import java.util.concurrent.Executors

import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.{Collectable, Debug, IndexedSet}
import models._
import rx.lang.scala.{Observable, Observer, Subscription}

import scala.collection.GenSeq

// Possible source of bugs: indexAll and apply(DirectoryRef) work on different threads. This solution is forced
// due to type erasure.
class MetadataCacher(mf: MusicFinder, songParser: String => Song, saver: JsonableSaver) extends SimpleActor[DirectoryRef] with Debug {
  import MetadataCacher._
  private def getDirectoryInfo(d: DirectoryRef, listener: () => Unit): DirectoryInfo = {
    val songs = mf.getSongFilePathsInDir(d).map(songParser)
    val album = songs.head.album
    listener()
    DirectoryInfo(songs, album, Artist(songs.head.artistName, Set(album)))
  }
  override def apply(dir: DirectoryRef) {
    val info = getDirectoryInfo(dir, () => ())
    saver.update[Song](_ ++ info.songs)
    saver.update[Album](_.toSet + info.album)
    saver.update[Artist](_./:(emptyArtistSet)(_ + _) + info.artist)
  }
  def indexAll(): Observable[IndexUpdate] = {
    import common.concurrency._
    def aux(obs: Observer[IndexUpdate]) {
      val updateQueue = Executors.newFixedThreadPool(1)
      MetadataCacher.this.queue.submit(() => {
          val dirs: GenSeq[DirectoryRef] = mf.albumDirs
          val totalSize = dirs.length
          var i = 0
          val $ = gatherInfo(dirs.map(d => getDirectoryInfo(d, () => {
            val j = i + 1
            updateQueue submit (() => obs onNext IndexUpdate(j, totalSize, d))
            i = j
          })))
          saver.save($.songs)
          saver.save($.albums)
          saver.save($.artists)
          obs.onCompleted()
        })
    }
    Observable apply aux
  }
}

class RealMetadataCacher extends MetadataCacher(RealLocations, f => Song(new File(f)), new JsonableSaver(RealLocations.dir))

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
}
