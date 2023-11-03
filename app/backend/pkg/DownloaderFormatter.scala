package backend.pkg

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import common.io.IODirectory
import controllers.{FileStreamFormatter, StreamResult, UrlPathUtils}

private class DownloaderFormatter @Inject() (
    ec: ExecutionContext,
    zipper: Zipper,
    urlPathUtils: UrlPathUtils,
    helper: FileStreamFormatter,
) {
  private implicit val iec: ExecutionContext = ec

  def apply(path: String, range: Option[String]): Future[StreamResult] = {
    val requestedFile = urlPathUtils.parseFile(path)
    require(requestedFile.isDirectory)
    zipper(IODirectory(requestedFile.getAbsolutePath))
      .map(zipFile =>
        helper(zipFile, "application/zip", range)
          .withHeaders("Content-Disposition" -> s"""attachment; filename="${zipFile.name}""""),
      )
  }
}
