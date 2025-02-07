package backend.external

import javax.inject.Inject

import controllers.{PlayActionConverter, PlayUrlDecoder}
import play.api.mvc.InjectedController

class ExternalController @Inject() (
    $ : ExternalFormatter,
    converter: PlayActionConverter,
) extends InjectedController {
  def get(path: String) = converter.ok($.get(PlayUrlDecoder(path)))
  def refreshArtist(path: String) = converter.ok($.refreshArtist(PlayUrlDecoder(path)))
  def refreshAlbum(path: String) = converter.ok($.refreshAlbum(PlayUrlDecoder(path)))
  def updateRecon(path: String) = converter.parseJson($.updateRecon(PlayUrlDecoder(path), _))
}
