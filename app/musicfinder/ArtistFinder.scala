package musicfinder

import com.google.inject.Inject
import mains.fixer.StringFixer
import models.ArtistName
import musicfinder.ArtistFinder.{DotSuffixes, IllegalWindowCharactersPattern}

import common.io.DirectoryRef
import common.rich.primitives.RichString._

class ArtistFinder @Inject() (mf: MusicFinder, stringFixer: StringFixer) {
  def apply(artist: ArtistName): Option[DirectoryRef] = {
    // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
    val canonicalArtistFolderName =
      stringFixer(artist).toLowerCase
        .removeAll(DotSuffixes)
        .removeAll(IllegalWindowCharactersPattern)

    println(s"finding matching folder for artist <$canonicalArtistFolderName>")
    mf.findArtistDir(backend.recon.Artist(canonicalArtistFolderName))
  }
}

private object ArtistFinder {
  // A windows folder name cannot end in '.'.
  private val DotSuffixes = """\.*$""".r.pattern
  // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
  private val IllegalWindowCharactersPattern = """[<>:"/\\|?*]""".r.pattern
}
