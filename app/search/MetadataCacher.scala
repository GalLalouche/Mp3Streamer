package search

import java.io.File

import common.concurrency.SimpleActor
import common.io.{DirectoryRef, FileRef}
import common.{Collectable, Debug, IndexedSet}
import controllers.RealLocations
import models._
import play.api.libs.json.{JsObject, Json}
import search.Jsonable._


class MetadataCacher(mf: MusicFinder, songParser: String => Song) extends SimpleActor[DirectoryRef] with Debug {

  private case class DirectoryInfo private(songs: Seq[Song], album: Album, artist: Artist)

  private object DirectoryInfo {
    def apply(d: DirectoryRef) = {
      val songs = mf.getSongFilePathsInDir(d).map(songParser)
      val album = songs.head.album
      val artist = new Artist(songs.head.artistName, Set(album))
      new DirectoryInfo(songs, album, artist)
    }
  }

  private case class AllInfo private(songs: Seq[Song], albums: List[Album], artists: IndexedSet[Artist]) {
    def this() = this(List(), List(), MetadataCacher.artistsSet)
    def +(other: DirectoryInfo) = new AllInfo(other.songs ++ songs, other.album :: albums, artists + other.artist)
  }

  private implicit object AllInfoCollectable extends Collectable[DirectoryInfo, AllInfo] {
    override def empty: AllInfo = new AllInfo
    override def +(s: AllInfo, t: DirectoryInfo): AllInfo = s + t
  }

  def indexAll() {
    val $ = Collectable.fromList(mf.albumDirs.map(DirectoryInfo(_)))
    save($.songs)
    save($.albums)
    save($.artists)
  }
  override def apply(dir: DirectoryRef) {
    val info = DirectoryInfo(dir)
    save(load[Song] ++ info.songs)
    save(load[Album].toSet + info.album)
    save(load[Artist]./:(MetadataCacher.artistsSet)(_ + _) + info.artist)
  }

  private def jsonFileName[T](m: Manifest[T]): FileRef =
    mf.dir addFile s"${m.runtimeClass.getSimpleName.replaceAll("\\$", "")}s.json"
  def save[T: Jsonable](data: Traversable[T])(implicit m: Manifest[T]) {
    require(data.nonEmpty, s"Can't save empty data of type <$m>")
    jsonFileName(m).write(data.map(implicitly[Jsonable[T]].jsonify).mkString("\n"))
  }
  def load[T: Jsonable](implicit m: Manifest[T]): Seq[T] =
    jsonFileName(m)
      .lines
      .map(Json.parse)
      .map(_.as[JsObject])
      .map(implicitly[Jsonable[T]].parse)
}

/**
  * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
  * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
  * production.
  */
object MetadataCacher extends MetadataCacher(RealLocations, f => Song(new File(f))) {
  val artistsSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name, _ merge _)
}
