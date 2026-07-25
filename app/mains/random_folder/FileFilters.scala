package mains.random_folder

import java.io.File

import backend.recon.Artist
import com.google.inject.Inject
import genre.{Genre, GenreFinder}
import genre.Genre.{Classical, Metal, NewAge}
import org.typelevel.ci.CIString
import play.api.libs.json.Json

import common.Filter
import common.json.RichJson.DynamicJson
import common.path.ref.io.{IODirectory, IOFile}
import common.rich.RichT.lazyT
import common.rich.collections.RichSet.richSet

private object FileFilters {
  private def removeGenres(genreFinder: GenreFinder, f: File)(
      g: PartialFunction[Genre, Boolean],
  ): Boolean = g.applyOrElse(genreFinder(IODirectory(f.getParent)), true.const)
  class SansMetal @Inject() (genreFinder: GenreFinder) extends Filter[IOFile] {
    override def passes(f: IOFile): Boolean = removeGenres(genreFinder, f) { case Metal(_) =>
      false
    }
  }
  class PartyDude @Inject() (genreFinder: GenreFinder) extends Filter[IOFile] {
    override def passes(f: IOFile): Boolean = removeGenres(genreFinder, f) {
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
      allowedArtists: Set[Artist],
      forbiddenArtists: Set[Artist],
      allowedAlbums: Set[CIString],
      forbiddenAlbums: Set[CIString],
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

      forbiddenGenres.doesNotContain(data.genre.name)
    }
  }
  def fromConfig(sde: SongDataExtractor): Filter[File] = {
    val json = Json.parse(getClass.getResourceAsStream("config.json"))
    def getSet[T](s: String, f: String => T) =
      json.array(s).value.view.map(f.compose(_.as[String])).toSet
    new FilterConfig(
      sde,
      forbiddenGenres = getSet("forbiddenGenres", identity),
      allowedArtists = getSet("allowedArtists", Artist.apply),
      forbiddenArtists = getSet("forbiddenArtists", Artist.apply),
      allowedAlbums = getSet("allowedAlbums", CIString.apply),
      forbiddenAlbums = getSet("forbiddenAlbums", CIString.apply),
    )
  }
}
