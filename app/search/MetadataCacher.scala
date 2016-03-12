package search

import java.io.File

import common.concurrency.SimpleActor
import common.io.DirectoryRef
import common.{Collectable, Debug, IndexedSet}
import controllers.RealLocations
import models._

class MetadataCacher(mf: MusicFinder, songParser: String => Song, saver: JsonableSaver)
  extends SimpleActor[DirectoryRef] with Debug {
  private case class DirectoryInfo private(songs: Seq[Song], album: Album, artist: Artist)
  private object DirectoryInfo {
    def apply(d: DirectoryRef) = {
      val songs = mf.getSongFilePathsInDir(d).map(songParser)
      val album = songs.head.album
      new DirectoryInfo(songs, album, new Artist(songs.head.artistName, Set(album)))
    }
  }
  private case class AllInfo(songs: Seq[Song], albums: List[Album], artists: IndexedSet[Artist])
  private implicit object AllInfoCollectable extends Collectable[DirectoryInfo, AllInfo] {
    override def empty: AllInfo = new AllInfo(List(), List(), MetadataCacher.artistsSet)
    override def +(agg: AllInfo, t: DirectoryInfo): AllInfo =
      new AllInfo(t.songs ++ agg.songs, t.album :: agg.albums, agg.artists + t.artist)
  }

  def indexAll() {
    println("indexing")
    val $ = Collectable.fromList(mf.albumDirs.map(DirectoryInfo(_)))
    saver.save($.songs)
    saver.save($.albums)
    saver.save($.artists)
  }
  override def apply(dir: DirectoryRef) {
    val info = DirectoryInfo(dir)
    saver.update[Song](_ ++ info.songs)
    saver.update[Album](_.toSet + info.album)
    saver.update[Artist](_./:(MetadataCacher.artistsSet)(_ + _) + info.artist)
  }
}

/**
  * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
  * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
  * production.
  */
object MetadataCacher extends MetadataCacher(RealLocations, f => Song(new File(f)), new JsonableSaver(RealLocations.dir)) {
  val artistsSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name, _ merge _)
}
