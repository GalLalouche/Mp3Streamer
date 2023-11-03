package mains.fixer

import javax.inject.Inject

import common.rich.path.Directory
import common.rich.primitives.RichString._
import models.IOMusicFinder

private class ArtistFinder @Inject() (
    mf: IOMusicFinder,
) {
  def apply(artist: String): Option[Directory] = {
    // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
    val canonicalArtistFolderName = StringFixer(artist).toLowerCase
      // A windows folder name cannot end in '.'.
      .removeAll("""\.*$""")
      // A windows folder name cannot contain '<', '>', ':', '"', '/', '\', '\', '|', '?', '*'.
      .removeAll("""[<>:"/\\|?*]""")

    println(s"finding matching folder for artist <$canonicalArtistFolderName>")
    mf.findArtistDir(canonicalArtistFolderName).map(_.dir)
  }
}
