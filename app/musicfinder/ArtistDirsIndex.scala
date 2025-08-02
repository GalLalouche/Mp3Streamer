package musicfinder

import backend.recon.Artist
import com.google.inject.{Inject, Singleton}
import models.ArtistDir

import scala.concurrent.ExecutionContext

import common.concurrency.AsyncVal
import common.io.{DirectoryRef, JsonableSaver}

@Singleton class ArtistDirsIndex @Inject() (
    saver: JsonableSaver,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  private val state = new AsyncVal(ArtistDirsIndexImpl.load(saver))
  def update(artistDirs: Iterable[ArtistDir]): Unit =
    state.set(ArtistDirsIndexImpl.from(artistDirs, saver))

  def forDir(dir: DirectoryRef): ArtistDirResult = state.get.forDir(dir)
  /** Returns `None` if there is no match. */
  def forArtist(artist: Artist): Option[DirectoryRef] = state.get.forArtist(artist)
}
