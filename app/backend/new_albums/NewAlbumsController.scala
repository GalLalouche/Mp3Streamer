package backend.new_albums

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayUrlDecoder}
import models.TypeAliases.ArtistName
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.std.option.optionInstance

/**
 * A web interface to new albums finder. Displays new albums and can update the current file /
 * ignoring policy.
 */
class NewAlbumsController @Inject() (
    $ : NewAlbumsFormatter,
    converter: PlayActionConverter,
    ec: ExecutionContext,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  def albums = converter.ok($.albums)
  def forArtist(artistName: ArtistName) = Action.async(
    $.forArtist(PlayUrlDecoder(artistName)).map(
      _.mapHeadOrElse(Ok(_), NotFound(s"Artist <$artistName> is not reconciled")),
    ),
  )

  def removeArtist(artist: ArtistName) = converter.noContent($.removeArtist(PlayUrlDecoder(artist)))
  def ignoreArtist(artist: ArtistName) = converter.noContent($.ignoreArtist(PlayUrlDecoder(artist)))
  def unignoreArtist(artist: ArtistName) =
    converter.noContent($.unignoreArtist(PlayUrlDecoder(artist)))

  def removeAlbum() = converter.parseJson($.removeAlbum)
  def ignoreAlbum() = converter.parseJson($.ignoreAlbum)

  def index = converter.html("new_albums")
}
