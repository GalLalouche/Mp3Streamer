package musicfinder

import com.google.inject.Inject
import mains.fixer.StringFixer
import models.ArtistName
import musicfinder.ArtistNameNormalizer._

import common.rich.primitives.RichString._

class ArtistNameNormalizer @Inject() (stringFixer: StringFixer) {
  def apply(name: ArtistName): String =
    stringFixer(name).removeAll(DotSuffixes).removeAll(IllegalWindowCharactersPattern)
}

private object ArtistNameNormalizer {
  // A windows folder name cannot end in '.'.
  private val DotSuffixes = """\.*$""".r.pattern
  // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
  private val IllegalWindowCharactersPattern = """[<>:"/\\|?*]""".r.pattern
}
