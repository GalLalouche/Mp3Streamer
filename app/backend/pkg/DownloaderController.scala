package backend.pkg

import javax.inject.Inject

import controllers.PlayActionConverter
import play.api.mvc.InjectedController

class DownloaderController @Inject() ($ : DownloaderFormatter, converter: PlayActionConverter)
    extends InjectedController {
  def download(path: String) = converter.parse(_.headers.get("Range"))($(path, _))
}
