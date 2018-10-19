package controllers

import akka.stream.scaladsl.Source
import akka.util.ByteString
import common.io.FileRef
import common.rich.primitives.RichString._
import javax.inject.Inject
import play.api.http.HttpEntity
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.mvc.{AnyContent, Request, Result, Results}

import scala.concurrent.ExecutionContext

class DownloaderHelper @Inject()(implicit ec: ExecutionContext) extends Results {
  def apply(file: FileRef, mimeType: String, request: Request[AnyContent]): Result = {
    // assumed format: [bytes=<start>-]
    def parseRange(s: String): Long = s.takeAfterLast('=').takeWhile(_.isDigit).toLong
    val bytesToSkip = request.headers get "Range" map parseRange getOrElse 0L
    val fis = file.inputStream
    fis.skip(bytesToSkip)
    val status = if (bytesToSkip == 0) Ok else PartialContent
    val source =
      Source.fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator fromStream fis)).map(ByteString.apply)
    status.sendEntity(HttpEntity.Streamed(source, Some(file.size - bytesToSkip), Some(mimeType))).withHeaders(
      "Access-Control-Allow-Headers" -> "range, accept-encoding",
      "Access-Control-Allow-Origin" -> "*",
      "Accept-Ranges" -> "bytes",
      "Connection" -> "close",
      "Content-Range" -> s"bytes $bytesToSkip-${file.size}/${file.size}",
    )
  }
}
