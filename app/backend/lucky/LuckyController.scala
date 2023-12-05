package backend.lucky

import javax.inject.Inject

import controllers.{PlayActionConverter, UrlDecodeUtils}
import play.api.mvc.InjectedController

class LuckyController @Inject() (
    $ : LuckyFormatter,
    converter: PlayActionConverter,
    urlDecodeUtils: UrlDecodeUtils,
) extends InjectedController {
  def search(query: String) = converter.ok($.search(urlDecodeUtils.decode(query)))
  def redirect(query: String) = converter.redirect($.search(urlDecodeUtils.decode(query)))
}
