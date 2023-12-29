package mains.random_folder

import java.io.File
import javax.inject.Inject

import models.{Genre, GenreFinder}
import models.Genre.{Classical, Metal, NewAge}
import play.api.libs.json.Json

import common.Filter
import common.io.{IODirectory, IOFile}
import common.json.RichJson.DynamicJson
import common.rich.primitives.RichBoolean.richBoolean

private object FileFilters {
  private def removeGenres(genreFinder: GenreFinder, f: File)(
      g: PartialFunction[Genre, Boolean],
  ): Boolean =
    // TODO RichPartialFunction.getOrElse
    g.lift(genreFinder(IODirectory(f.getParent))).getOrElse(true)
  class SansMetal @Inject() (genreFinder: GenreFinder) extends Filter[IOFile] {
    override def passes(f: IOFile): Boolean = removeGenres(genreFinder, f.file) { case Metal(_) =>
      false
    }
  }
  class PartyDude @Inject() (genreFinder: GenreFinder) extends Filter[IOFile] {
    override def passes(f: IOFile): Boolean = removeGenres(genreFinder, f.file) {
      case Metal(_) => false
      case Classical | NewAge => false
    }
  }
  object AllowEverything extends Filter[File] {
    override def passes(f: File): Boolean = true
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
  ) extends Filter[File] {
    override def passes(f: File): Boolean = {
      val data = sde(f)
      if (allowedAlbums(data.album))
        return true
      if (forbiddenAlbums(data.album))
        return false

      if (allowedArtists(data.artist))
        return true
      if (forbiddenArtists(data.artist))
        return false

      // TODO RichSet.doesNotContain
      forbiddenGenres.contains(data.genre.name).isFalse
    }
  }
  def fromConfig(sde: SongDataExtractor): Filter[File] = {
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
