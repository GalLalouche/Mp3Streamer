package backend.external

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import play.api.mvc.InjectedController

class ExternalController @Inject() (
    $ : ExternalFormatter,
    converter: PlayActionConverter,
    decoder: UrlDecodeUtils,
) extends InjectedController {
  def get(path: String) = converter.ok($.get(decoder.decode(path)))
  def refreshArtist(path: String) = converter.ok($.refreshArtist(decoder.decode(path)))
  def refreshAlbum(path: String) = converter.ok($.refreshAlbum(decoder.decode(path)))
  def updateRecon(path: String) = converter.parseJson($.updateRecon(decoder.decode(path), _))
}
