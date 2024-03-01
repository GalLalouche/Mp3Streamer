package backend.albums.filler

import javax.inject.Inject

import backend.recon.{Artist, ArtistReconStorage, ReconID}
import backend.recon.StoredReconResult.HasReconResult
import models.TypeAliases.ArtistName

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.Scalaz.ToBindOps

// Easier (and safer!) than opening SQLiteBrowser!
private class ArtistReconPusherImpl @Inject() (
    ec: ExecutionContext,
    storage: ArtistReconStorage,
    verifier: ArtistReconVerifier,
) extends ArtistReconPusher {
  implicit val iec: ExecutionContext = ec

  private def go(
      name: ArtistName,
      musicBrainzId: String,
      isIgnored: Boolean,
      validateAlbums: Boolean,
  ): Future[Unit] = {
    val artist = Artist(name)
    val reconID = ReconID.validateOrThrow(musicBrainzId)
    val isValid = if (validateAlbums) verifier(artist, reconID) else Future.successful(true)
    isValid
      .filterWithMessage(identity, s"Could not validate <$name> with ID <$musicBrainzId>")
      .>>(storage.store(artist, HasReconResult(reconID, isIgnored)))
  }

  override def withValidation(
      name: ArtistName,
      reconId: String,
      isIgnored: Boolean,
  ): Future[Unit] =
    go(
      name = name,
      musicBrainzId = reconId,
      isIgnored = isIgnored,
      validateAlbums = true,
    )
  /**
   * Does not perform validation, since sometimes MusicBrainz has incorrect album definitions that
   * is too annoying to fix.
   */
  def force(name: ArtistName, reconId: String, isIgnored: Boolean): Future[Unit] =
    go(
      name = name,
      musicBrainzId = reconId,
      isIgnored = isIgnored,
      validateAlbums = false,
    )
}
