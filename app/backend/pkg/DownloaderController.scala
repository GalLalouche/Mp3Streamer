package backend.pkg

import common.io.IODirectory
import controllers.{DownloaderHelper, UrlPathUtils}
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class DownloaderController @Inject()(
    ec: ExecutionContext,
    helper: DownloaderHelper,
    zipper: Zipper,
    urlPathUtils: UrlPathUtils,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def download(path: String) = Action.async {request =>
    val requestedFile = urlPathUtils.parseFile(path)
    require(requestedFile.isDirectory)
    zipper(IODirectory(requestedFile.getAbsolutePath))
        .map(zipFile => helper(zipFile, "application/zip", request)
            .withHeaders("Content-Disposition" -> s"""attachment; filename="${zipFile.name}""""))
  }
}
