package genre

import backend.recon.Artist
import com.google.inject.Inject
import genre.Genre.Classical

import common.path.ref.DirectoryRef
import common.rich.primitives.RichString._

class GenreFinder @Inject() (stringGenreFinder: StringGenreFinder) {
  def forArtist(artist: Artist): Option[Genre] = stringGenreFinder.forArtist(artist).map(from)

  def apply(dir: DirectoryRef): Genre = from(stringGenreFinder.forDir(dir))
  def isClassical(dir: DirectoryRef): Boolean = dir.parents.exists(_.name == Classical.name)

  import Genre._

  private def from(g: StringGenre): Genre = g match {
    case StringGenre.Flat(name) =>
      name match {
        case ciMatch"Blues" => Blues
        case ciMatch"Jazz" => Jazz
        case ciMatch"Musicals" => Musicals
        case ciMatch"New Age" => NewAge
        case ciMatch"Classical" => Classical
        case e => throw new NoSuchElementException(e)
      }
    case StringGenre.Nested(top, sub) =>
      top match {
        case ciMatch"Rock" => Rock(sub)
        case ciMatch"Metal" => Metal(sub)
        case _ => throw new NoSuchElementException(top)
      }
  }
}
