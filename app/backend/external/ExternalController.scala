package backend.external

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import play.api.mvc.InjectedController

class ExternalController @Inject() (
    $ : ExternalFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  def get(path: String) = converter.ok($.get(decoder(path)))
  def refreshArtist(path: String) = converter.ok($.refreshArtist(decoder(path)))
  def refreshAlbum(path: String) = converter.ok($.refreshAlbum(decoder(path)))
  def updateRecon(path: String) = converter.parseJson($.updateRecon(decoder(path), _))
}
