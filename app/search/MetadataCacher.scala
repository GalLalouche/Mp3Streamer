package search

import java.io.File

import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.{Collectable, Debug, IndexedSet}
import controllers.{RealLocations, Searcher}
import models._

import scala.collection.GenSeq

class MetadataCacher(mf: MusicFinder, songParser: String => Song, saver: JsonableSaver)
  extends SimpleActor[DirectoryRef] with Debug {
  import MetadataCacher._
  private def getDirectoryInfo(d: DirectoryRef): DirectoryInfo = {
    val songs = mf.getSongFilePathsInDir(d).map(songParser)
    val album = songs.head.album
    new DirectoryInfo(songs, album, new Artist(songs.head.artistName, Set(album)))
  }
  def indexAll() {
    println("indexing")
    val $ = gatherInfo(mf.albumDirs.map(getDirectoryInfo))
    saver.save($.songs)
    saver.save($.albums)
    saver.save($.artists)
  }
  override def apply(dir: DirectoryRef) {
    val info = getDirectoryInfo(dir)
    saver.update[Song](_ ++ info.songs)
    saver.update[Album](_.toSet + info.album)
    saver.update[Artist](_./:(emptyArtistSet)(_ + _) + info.artist)
    Searcher.!
  }
}

/**
 * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
 * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
 * production.
 */
object MetadataCacher extends MetadataCacher(RealLocations, f => Song(new File(f)), new JsonableSaver(RealLocations.dir)) {
  val emptyArtistSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name, _ merge _)
  case class AllInfo(songs: Seq[Song], albums: List[Album], artists: IndexedSet[Artist])
  case class DirectoryInfo(songs: Seq[Song], album: Album, artist: Artist)
  implicit object AllInfoCollectable extends Collectable[DirectoryInfo, AllInfo] {
    override def empty: AllInfo = new AllInfo(List(), List(), emptyArtistSet)
    override def +(agg: AllInfo, t: DirectoryInfo): AllInfo =
      new AllInfo(t.songs ++ agg.songs, t.album :: agg.albums, agg.artists + t.artist)
  }
  def gatherInfo($: GenSeq[DirectoryInfo]) = Collectable fromList $
}
