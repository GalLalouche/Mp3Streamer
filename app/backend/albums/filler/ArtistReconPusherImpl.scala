package backend.albums.filler

import backend.recon.{Artist, ArtistReconStorage, ReconID}
import backend.recon.StoredReconResult.HasReconResult
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import scalaz.Scalaz.ToBindOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._

// Easier (and safer!) than opening SQLiteBrowser!
private class ArtistReconPusherImpl @Inject() (
    ec: ExecutionContext,
    storage: ArtistReconStorage,
    verifier: ArtistReconVerifier,
) extends ArtistReconPusher {
  implicit val iec: ExecutionContext = ec

  private def go(
      artistName: String,
      musicBrainzId: String,
      isIgnored: Boolean,
      validateAlbums: Boolean,
  ): Future[Unit] = {
    val artist = Artist(artistName)
    val reconID = ReconID.validateOrThrow(musicBrainzId)
    val isValid = if (validateAlbums) verifier(artist, reconID) else Future.successful(true)
    isValid
      .filterWithMessage(identity, s"Could not validate <$artistName> with ID <$musicBrainzId>")
      .>>(storage.store(artist, HasReconResult(reconID, isIgnored)))
  }

  override def withValidation(
      artistName: String,
      reconId: String,
      isIgnored: Boolean,
  ): Future[Unit] =
    go(
      artistName = artistName,
      musicBrainzId = reconId,
      isIgnored = isIgnored,
      validateAlbums = true,
    )
  /**
   * Does not perform validation, since sometimes MusicBrainz has incorrect album definitions that
   * is too annoying to fix.
   */
  def force(artistName: String, reconId: String, isIgnored: Boolean): Future[Unit] =
    go(
      artistName = artistName,
      musicBrainzId = reconId,
      isIgnored = isIgnored,
      validateAlbums = false,
    )
}
