package search

import java.io.File

import scala.annotation.migration

import Jsonable._
import common.concurrency.SimpleActor
import common.rich.collections.RichSeq.richSeq
import common.rich.path.RichFile.richFile
import models.{ Album, Artist, MusicFinder, Song }
import play.api.libs.json.{ JsObject, Json }

object MetadataCacher extends SimpleActor[MusicFinder] {
  private case class FileMetadata(song: Song, album: Album, artist: String)
  private case class Cacheables private (songs: List[Song], albums: Set[Album], artists: Map[String, Artist]) {
    def this() = this(Nil, Set(), Map().withDefault(a => new Artist(a, Set())))
    def +(fm: FileMetadata) = {
      val updatedArtists = {
        val a = artists(fm.artist)
        artists + (a.name -> a.addAlbum(fm.album))
      }
      Cacheables(fm.song :: songs, albums + fm.album, updatedArtists)
    }
  }
  private def jsonFileName[T](m: Manifest[T]) =
    new File(s"D:/Media/Music/${m.runtimeClass.getSimpleName.replaceAll("\\$", "")}.json")
  private def extractMetadataFromFile(path: String): FileMetadata = {
    val song = Song(new File(path))
    FileMetadata(song, song.album, song.artistName)
  }
  private def extractMetadata(mf: MusicFinder): Cacheables = {
    mf.getSongFilePaths.par.foldLeft(new Cacheables)((c, f) => c + (extractMetadataFromFile(f)))
  }
  override def apply(mf: MusicFinder) {
    val $ = extractMetadata(mf)
    save($.songs)
    save($.albums.toSeq)
    save($.artists.values.toSeq)
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