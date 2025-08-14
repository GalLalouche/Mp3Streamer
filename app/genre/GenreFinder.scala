package genre

import com.google.inject.Inject
import genre.Genre.Classical

import common.io.DirectoryRef

class GenreFinder @Inject() (stringGenreFinder: StringGenreFinder) {
  def forArtist(artist: backend.recon.Artist): Option[Genre] =
    stringGenreFinder.forArtist(artist).map(from)

  def apply(dir: DirectoryRef): Genre = from(stringGenreFinder.forDir(dir))
  def isClassical(dir: DirectoryRef): Boolean = dir.parents.exists(_.name == Classical.name)

  import Genre._

  private def from(g: StringGenre): Genre = g match {
    case StringGenre.Flat(name) =>
      name match {
        case "Blues" => Blues
        case "Jazz" => Jazz
        case "Musicals" => Musicals
        case "New Age" => NewAge
        case "Classical" => Classical
        case e => throw new NoSuchElementException(e)
      }
    case StringGenre.Nested(top, sub) =>
      top.toLowerCase match {
        case "rock" => Rock(sub)
        case "metal" => Metal(sub)
        case _ => throw new NoSuchElementException(top)
      }
  }
}
