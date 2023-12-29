package backend.albums.filler

import javax.inject.Inject

import backend.recon.{Album, AlbumReconStorage, ReconID}
import backend.recon.StoredReconResult.HasReconResult

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import scalaz.Scalaz.{ToBindOps, ToFunctorOps}

import common.rich.RichFuture._

// Easier (and safer!) than opening SQLiteBrowser!
// TODO handle duplication with ArtistReconPusher
private class AlbumReconPusher @Inject() (
    ec: ExecutionContext,
    storage: AlbumReconStorage,
    verifier: AlbumReconVerifier,
) {
  implicit val iec: ExecutionContext = ec

  private def go(album: Album, musicBrainzId: String, validate: Boolean): Unit = {
    val reconID = ReconID.validateOrThrow(musicBrainzId)
    val isValid = if (validate) verifier(album, reconID) else Future.successful(true)
    isValid
      .filterWithMessage(identity, s"Could not validate <$album> with ID <$musicBrainzId>")
      .>>(storage.store(album, HasReconResult(reconID, isIgnored = false)))
      .>|(println("done"))
      .get
  }

  def withValidation(album: Album, reconId: String): Unit = go(album, reconId, validate = true)
  /**
   * Does not perform validation, since sometimes MusicBrainz has incorrect album definitions that
   * is too annoying to fix.
   */
  def force(album: Album, reconId: String): Unit = go(album, reconId, validate = false)
}
