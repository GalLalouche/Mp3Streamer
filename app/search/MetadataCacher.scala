package search

import java.io.File

import common.concurrency.SimpleActor
import common.io.{FileSystem, IOFileSystem}
import common.rich.RichT._
import common.{Debug, IndexedSet}
import controllers.Searcher
import models._
import play.api.libs.json.{JsObject, Json}
import search.Jsonable._


class MetadataCacher extends SimpleActor[Seq[String]] with Debug {
  self: FileSystem =>
  private case class FileMetadata(song: Song, album: Album, artist: Artist)

  private class Cacheables private(val songs: List[Song], val albums: Set[Album], val artists: IndexedSet[Artist]) {
    def this() = this(Nil, Set(), MetadataCacher.artistsSet)
    def +(fm: FileMetadata) = new Cacheables(fm.song :: songs, albums + fm.album, artists + fm.artist)
  }

  private def jsonFileName[T](m: Manifest[T]) =
    getFile(s"D:/Media/Music/${m.runtimeClass.getSimpleName.replaceAll("\\$", "") }s.json")
  // override in test
  protected def getSong(path: String): Song = Song(new File(path))
  private def extractMetadata(i: Seq[String]): Cacheables = {
    def extractMetadataFromFile(path: String): FileMetadata =
      getSong(path).mapTo(song => FileMetadata(song, song.album, new Artist(song.artistName, Set(song.album))))
    i.foldLeft(new Cacheables)(_ + extractMetadataFromFile(_))
  }
  def indexAll(mf: MusicFinder) {
    val $ = extractMetadata(mf.getSongFilePaths)
    save($.songs)
    save($.albums)
    save($.artists)
  }
  override def apply(newFiles: Seq[String]) {
    def update(c: Cacheables) {
      save(load[Song] ++ c.songs)
      save(load[Album].toSet ++ c.albums)
      // Gonna curse myself in a few months, but totally worth it
      save[Artist](c.artists./:(load[Artist]./:(MetadataCacher.artistsSet)(_ + _))(_ + _))
    }
    update(extractMetadata(newFiles))
    Searcher !
  }

  def save[T: Jsonable](data: Traversable[T])(implicit m: Manifest[T]) {
    require(data.nonEmpty, s"Can't save empty data of type <$m>")
    val f = jsonFileName(m)
    f.create()
    f.write(data.map(implicitly[Jsonable[T]].jsonify).mkString("\n"))
  }
  def load[T: Jsonable](implicit m: Manifest[T]): Seq[T] = {
    val file = jsonFileName(m)
    if (file.exists == false)
      return Nil // for testing, if there is nothing to load; shouldn't happen in production
    val lines = file.lines
    assert(file.lines.nonEmpty && lines.head != "", s"Json file <$file> exists but is empty")
    lines
      .map(Json.parse)
      .map(_.as[JsObject])
      .map(implicitly[Jsonable[T]].parse)
  }
}

/**
  * Caches all music metadata on disk. Since extracting the metadata requires processing hundreds of gigabytes, but
  * the actual metadata is only in megabytes. Also allows for incremental updates, in the case of new data added during
  * production.
  */
object MetadataCacher extends MetadataCacher with IOFileSystem {
  val artistsSet: IndexedSet[Artist] = IndexedSet[String, Artist](_.name, _ merge _)
}
