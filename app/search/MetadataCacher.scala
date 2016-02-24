package search

import models.MusicFinder
import common.concurrency.SimpleActor
import models.Song
import common.rich.RichT._
import java.io.File
import common.rich.path.RichFile._
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import Jsonable._
import models.MusicFinder
import controllers.Searcher
import models.Album
import models.Artist
import play.api.libs.json.JsObject
import common.rich.path.Directory

object MetadataCacher extends SimpleActor[MusicFinder] {

  private def jsonFileName[T](cls: Manifest[T]) = new File(s"D:/Media/Music/${cls.runtimeClass.getSimpleName.replaceAll("\\$", "")}.json")
  private case class FileMetadata(song: Song, album: Album, artist: Artist)
  private case class Cacheables(songs: List[Song], albums: Set[Album], artists: Set[Artist]) {
    def this() = this(Nil, Set(), Set())
    def +(fm: FileMetadata) = Cacheables(fm.song :: songs, albums + fm.album, artists + fm.artist)
  }
  private def extractMetadataFromFile(path: String): FileMetadata = {
    val song = Song(new File(path))
    FileMetadata(song, song.album, song.album.artist)
  }
  private def extractMetadata(mf: MusicFinder): Cacheables = {
    mf.getSongFilePaths.par.foldLeft(new Cacheables)((c, f) => c + (extractMetadataFromFile(f)))
  }
  override def apply(mf: MusicFinder) {
    val $ = extractMetadata(mf)
    save($.songs)
    save($.albums.toSeq)
    save($.artists.toSeq)
  }
  def save[T: Jsonable](data: Seq[T])(implicit m: Manifest[T]) {
    val f = jsonFileName(m)
    f.createNewFile()
    f.write(data.map(implicitly[Jsonable[T]].jsonify).mkString("\n"))
  }
  def load[T: Jsonable](implicit m: Manifest[T]): Seq[T] = jsonFileName(m)
    .lines
    .map(Json.parse)
    .map(_.as[JsObject])
    .map(implicitly[Jsonable[T]].parse)
}