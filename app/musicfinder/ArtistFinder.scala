package musicfinder

import com.google.inject.Inject
import models.ArtistName

import common.io.DirectoryRef

class ArtistFinder @Inject() (mf: MusicFinder, artistNameNormalizer: ArtistNameNormalizer) {
  def apply(name: ArtistName): Option[DirectoryRef] = {
    // See https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
    val canonicalArtistFolderName = artistNameNormalizer(name).toLowerCase

    println(s"finding matching folder for artist <$canonicalArtistFolderName>")
    mf.findArtistDir(backend.recon.Artist(canonicalArtistFolderName))
  }
}
