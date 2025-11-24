package musicfinder

import backend.recon.Artist
import com.google.inject.{Inject, Singleton}
import genre.GenreFinder
import models.ArtistDir

import common.io.{DirectoryRef, PathRefFactory}
import common.json.saver.JsonableCOWFactory

@Singleton class ArtistDirsIndex @Inject() (
    cowFactory: JsonableCOWFactory,
    genreFinder: GenreFinder,
    pathRefFactory: PathRefFactory,
) {
  def update(artistDirs: Iterable[ArtistDir]): Unit = state.set(artistDirs)
  def forDir(dir: DirectoryRef): ArtistDirResult = state.get.forDir(dir)
  /** Returns `None` if there is no match. */
  def forArtist(artist: Artist): Option[DirectoryRef] = state.get.forArtist(artist)

  private implicit val ifactory: PathRefFactory = pathRefFactory
  private val state = ArtistDirsIndexImpl.persistentValue(genreFinder, cowFactory)
}
