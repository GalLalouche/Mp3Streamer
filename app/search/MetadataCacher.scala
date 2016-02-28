package search

import java.io.File
import scala.annotation.migration
import Jsonable._
import common.concurrency.SimpleActor
import common.rich.collections.RichSeq.richSeq
import common.rich.path.RichFile.richFile
import models.{ Album, Artist, MusicFinder, Song }
import play.api.libs.json.{ JsObject, Json }
import loggers.CompositeLogger
import common.rich.path.Directory
import controllers.Searcher
import common.Debug

object MetadataCacher extends SimpleActor[File] with Debug {
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
    new File(s"D:/Media/Music/${m.runtimeClass.getSimpleName.replaceAll("\\$", "")}s.json")
  private def extractMetadataFromFile(path: String): FileMetadata = {
    val song = Song(new File(path))
    FileMetadata(song, song.album, song.artistName)
  }
  private def extractMetadata(i: Seq[String]): Cacheables = {
    i.par.foldLeft(new Cacheables)((c, f) => c + (extractMetadataFromFile(f)))
  }
  def indexAll(mf: MusicFinder) {
    val $ = extractMetadata(mf.getSongFilePaths)
    save($.songs)
    save($.albums.toSeq)
    save($.artists.values.toSeq)
  }
  override def apply(dir: File) {
    def update(c: Cacheables) {
      save(load[Song] ++ c.songs)
      save((load[Album].toSet ++ (c.albums)).toSeq)
      save(load[Artist].map(a =>
        new Artist(a.name, c.artists.get(a.name).map(a.albums.toSet ++ _.albums).getOrElse(a.albums.toSet))))
      Searcher!
    }
    if (dir.exists) // new Directory added
      update(extractMetadata(Directory(dir).files.filter(e => Set("flac", "mp3").contains(e.extension.toLowerCase)).map(_.path)))
    else // Directory deleted. Please don't do that :|
      CompositeLogger.warn("a directory has been deleted; please rerun the entire indexing")
  }
  def save[T: Jsonable](data: Seq[T])(implicit m: Manifest[T]) {
    val f = jsonFileName(m)
    f.createNewFile()
    val json = timed("JsonifyingData") { data.map(implicitly[Jsonable[T]].jsonify).mkString("\n") }
    f.write(json)
  }
  def load[T: Jsonable](implicit m: Manifest[T]): Seq[T] = {
    val file = jsonFileName(m)
    if (file.exists == false)
      return Nil // for testing, if there is nothing to load; shouldn't happen in production
    file
      .lines
      .map(Json.parse)
      .map(_.as[JsObject])
      .map(implicitly[Jsonable[T]].parse)
  }
}