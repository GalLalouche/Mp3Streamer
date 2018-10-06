package backend.pkg

import controllers.FormatterUtils
import javax.inject.Inject
import play.api.mvc.InjectedController

class DownloaderController @Inject()($: DownloaderFormatter, formatterUtils: FormatterUtils)
    extends InjectedController {
  def download(path: String) = formatterUtils.parse(_.headers get "Range")($(path, _))
}
