package backend.search

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.mvc.InjectedController

class SearchController @Inject() ($ : SearchFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def search(path: String) = converter.ok($.search(path))
}
