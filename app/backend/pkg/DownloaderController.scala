package backend.pkg

import java.io.FileInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import common.io.{IODirectory, IOFile}
import common.rich.primitives.RichString._
import controllers.{ControllerUtils, LegacyController}
import play.api.http.HttpEntity
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.mvc.Action

object DownloaderController extends LegacyController {
  private val zipper = new Zipper(ControllerUtils.encodePath)

  def download(path: String) = Action.async {request =>
    // TODO fix code duplication with streamer
    def parseRange(s: String): Long = s.dropAfterLast('=').takeWhile(_.isDigit).toLong
    val bytesToSkip: Long = request.headers get "Range" map parseRange getOrElse 0L
    val file = ControllerUtils.parseFile(path)
    require(file.isDirectory)
    zipper.zip(IODirectory(file.getAbsolutePath))
        .map(file => {
          val fis = new FileInputStream(file.asInstanceOf[IOFile].file)
          fis.skip(bytesToSkip)
          val status = if (bytesToSkip == 0) Ok else PartialContent
          val source =
            Source.fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator fromStream fis)).map(ByteString.apply)
          status.sendEntity(HttpEntity.Streamed(source, Some(file.size - bytesToSkip), Some("application/zip"))).withHeaders(
            "Access-Control-Allow-Headers" -> "range, accept-encoding",
            "Access-Control-Allow-Origin" -> "*",
            "Accept-Ranges" -> "bytes",
            "Connection" -> "keep-alive",
            //"Content-Length" -> s"${}",
            "Content-Range" -> s"bytes $bytesToSkip-${file.size}/${file.size}",
            "Content-Disposition" -> s"attachment; filename=${file.name}",
          )
        })
  }
}
