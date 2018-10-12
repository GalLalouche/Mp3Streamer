package backend.search

import controllers.PlayActionConverter
import javax.inject.Inject
import play.api.mvc.InjectedController

class SearchController @Inject()($: SearchFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def search(path: String) = converter.ok($.search(path))
}
