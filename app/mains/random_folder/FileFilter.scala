package mains.random_folder

import java.io.File

import javax.inject.Inject
import play.api.libs.json.Json

import common.json.RichJson._
import common.rich.primitives.RichBoolean._

private trait FileFilter {
  def isAllowed(f: File): Boolean
}

private object FileFilter {
  class SansMetal @Inject()(sde: SongDataExtractor) extends FileFilter {
    override def isAllowed(f: File): Boolean = sde(f).majorGenre != "metal"
  }
  object AllowEverything extends FileFilter {
    override def isAllowed(f: File): Boolean = true
  }
  // The general semantics is that every level can override the the level above it, so an allowed album
  // overrides forbidden artist, allowed artists overrides forbidden genre.
  private class FilterConfig(
      sde: SongDataExtractor,

      forbiddenGenres: Set[String],

      allowedArtists: Set[String],
      forbiddenArtists: Set[String],

      allowedAlbums: Set[String],
      forbiddenAlbums: Set[String],
  ) extends FileFilter {
    override def isAllowed(f: File): Boolean = {
      val data = sde(f)
      if (allowedAlbums(data.album))
        return true
      if (forbiddenAlbums(data.album))
        return false

      if (allowedArtists(data.artist))
        return true
      if (forbiddenArtists(data.artist))
        return false

      forbiddenGenres(data.genre).isFalse
    }
  }
  def fromConfig(sde: SongDataExtractor): FileFilter = {
    val json = Json.parse(getClass.getResourceAsStream("config.json"))
    def getSet(s: String): Set[String] = json.array(s).value.view.map(_.as[String]).toSet
    new FilterConfig(
      sde,

      forbiddenGenres = getSet("forbiddenGenres"),

      allowedArtists = getSet("allowedArtists"),
      forbiddenArtists = getSet("forbiddenArtists"),

      allowedAlbums = getSet("allowedAlbums"),
      forbiddenAlbums = getSet("forbiddenAlbums"),
    )
  }
}
