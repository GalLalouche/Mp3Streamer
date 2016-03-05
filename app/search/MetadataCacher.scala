package search

import java.io.File

import common.Debug
import common.concurrency.SimpleActor
import common.io.{FileSystem, IOFileSystem}
import common.rich.RichT._
import common.rich.collections.RichSeq._
import controllers.Searcher
import models._
import play.api.libs.json.{JsObject, Json}
import search.Jsonable._


class MetadataCacher extends SimpleActor[Seq[String]] with Debug {
  self: FileSystem =>

  private implicit class RichMap[Key, T](map: Map[Key, T]) {
    def update(s: Key, f: T => T) = map.updated(s, f(map(s)))
  }
  private case class FileMetadata(song: Song, album: Album, artist: String)

  private case class Cacheables private(songs: List[Song], albums: Set[Album], artists: Map[String, Artist]) {
    def this() = this(Nil, Set(), Map().withDefault(a => new Artist(a, Set())))
    def +(fm: FileMetadata) =
      Cacheables(fm.song :: songs, albums + fm.album, mergeArtists(new Artist(fm.artist, Set(fm.album)), artists))
  }

  private def mergeArtists(a: Artist, artists: Map[String, Artist]): Map[String, Artist] = {
    if (artists contains a.name)
      artists.update(a.name, _.merge(a))
    else
      artists + (a.name -> a)
  }
  private def mergeArtistMaps(smallMap: Map[String, Artist], largeMap: Map[String, Artist]) = {
    smallMap.values.foldRight(largeMap)(mergeArtists)
  }

  private def jsonFileName[T](m: Manifest[T]) =
    getFile(s"D:/Media/Music/${m.runtimeClass.getSimpleName.replaceAll("\\$", "") }s.json")
  // override in test
  protected def getSong(path: String): Song = Song(new File(path))
  private def extractMetadata(i: Seq[String]): Cacheables = {
    def extractMetadataFromFile(path: String): FileMetadata =
      getSong(path).mapTo(song => FileMetadata(song, song.album, song.artistName))
    i.par.foldLeft(new Cacheables)(_ + extractMetadataFromFile(_))
  }
  def indexAll(mf: MusicFinder) {
    val $ = extractMetadata(mf.getSongFilePaths)
    save($.songs)
    save($.albums.toSeq)
    save($.artists.values.toSeq)
  }
  override def apply(newFiles: Seq[String]) {
    def update(c: Cacheables) {
      save(load[Song] ++ c.songs)
      save((load[Album].toSet ++ (c.albums)).toSeq)
      save(mergeArtistMaps(c.artists, load[Artist].zipMap(_.name).map(_.swap).toMap).values.toSeq)
    }
    update(extractMetadata(newFiles))
    Searcher !
  }

  def save[T: Jsonable](data: Seq[T])(implicit m: Manifest[T]) {
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
object MetadataCacher extends MetadataCacher with IOFileSystem
