package models

import javax.inject.Inject

import mains.fixer.StringFixer
import models.ArtistFinder.{EndsInDotPattern, IllegalWindowCharactersPattern}

import common.io.DirectoryRef
import common.rich.primitives.RichString._

class ArtistFinder @Inject() (mf: MusicFinder, stringFixer: StringFixer) {
  def apply(artist: String): Option[DirectoryRef] = {
    // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
    val canonicalArtistFolderName =
      stringFixer(artist).toLowerCase
        .removeAll(EndsInDotPattern)
        .removeAll(IllegalWindowCharactersPattern)

    println(s"finding matching folder for artist <$canonicalArtistFolderName>")
    mf.findArtistDir(canonicalArtistFolderName)
  }
}

private object ArtistFinder {
  // A windows folder name cannot end in '.'.
  private val EndsInDotPattern = """\.*$""".r.pattern
  // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
  private val IllegalWindowCharactersPattern = """[<>:"/\\|?*]""".r.pattern
}
