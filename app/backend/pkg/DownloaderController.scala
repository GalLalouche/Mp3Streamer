package backend.pkg

import common.io.IODirectory
import common.rich.path.RichFile._
import controllers.{DownloaderHelper, UrlPathUtils}
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class DownloaderController @Inject()(
    ec: ExecutionContext,
    helper: DownloaderHelper,
    zipper: Zipper,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec

  def download(path: String) = Action.async {request =>
    val file = UrlPathUtils.parseFile(path)
    require(file.isDirectory)
    zipper(IODirectory(file.getAbsolutePath))
        .map(helper(_, "application/zip", request)
            .withHeaders("Content-Disposition" -> s"""attachment; filename="${file.name}""""))
  }
}
