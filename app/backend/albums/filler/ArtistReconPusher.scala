package backend.albums.filler

import backend.recon.{Artist, ArtistReconStorage, ReconID}
import backend.recon.StoredReconResult.HasReconResult
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.rich.RichFuture._

// Easier (and safer!) than opening SQLiteBrowser!
private class ArtistReconPusher @Inject()(
    ec: ExecutionContext,
    storage: ArtistReconStorage,
    verifier: ArtistReconVerifier,
) {
  implicit val iec: ExecutionContext = ec

  private def go(
      artistName: String, musicBrainzId: String, isIgnored: Boolean, validateAlbums: Boolean): Unit = {
    val artist = Artist(artistName)
    val reconID = ReconID.validateOrThrow(musicBrainzId)
    (for {
      isValid <- if (validateAlbums) verifier(artist, reconID) else Future.successful(true)
      if isValid
      _ <- storage.store(artist, HasReconResult(reconID, isIgnored))
      _ = println("Done!")
    } yield ()).get
  }

  def withValidation(artistName: String, reconId: String, isIgnored: Boolean): Unit =
    go(artistName = artistName, musicBrainzId = reconId, isIgnored = isIgnored, validateAlbums = true)
  /**
  * Does not perform validation, since sometimes MusicBrainz has incorrect album definitions that is too
  * annoying to fix.
  */
  def force(artistName: String, reconId: String, isIgnored: Boolean): Unit =
    go(artistName = artistName, musicBrainzId = reconId, isIgnored = isIgnored, validateAlbums = false)
}

