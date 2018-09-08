package backend.pkg

import backend.Retriever
import common.io.{DirectoryRef, FileRef, IODirectory}
import common.rich.path.RichFile._
import controllers.{ControllerUtils, DownloaderHelper}
import javax.inject.Inject
import play.api.mvc.InjectedController

import scala.concurrent.ExecutionContext

class DownloaderController @Inject()(
    ec: ExecutionContext,
    zipperFactory: ZipperFactory,
    helper: DownloaderHelper,
) extends InjectedController {
  private implicit val iec: ExecutionContext = ec
  private val zipper: Retriever[DirectoryRef, FileRef] = zipperFactory(ControllerUtils.encodePath)

  def download(path: String) = Action.async {request =>
    val file = ControllerUtils.parseFile(path)
    require(file.isDirectory)
    zipper(IODirectory(file.getAbsolutePath))
        .map(helper(_, "application/zip", request)
            .withHeaders("Content-Disposition" -> s"""attachment; filename="${file.name}""""))
  }
}
