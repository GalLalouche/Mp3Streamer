package controllers

import java.net.HttpURLConnection

import common.io.FileRef
import common.rich.func.ToMoreFoldableOps
import common.rich.primitives.RichString._

import scalaz.std.OptionInstances

class FileStreamFormatter
    extends ToMoreFoldableOps with OptionInstances {
  def apply(file: FileRef, mimeType: String, range: Option[String]): StreamResult = {
    // assumed format: [bytes=<start>-]
    def parseRange(s: String): Long = s.dropAfterLast('=').takeWhile(_.isDigit).toLong
    val bytesToSkip = range.mapHeadOrElse(parseRange, 0L)
    val fis = file.inputStream
    fis.skip(bytesToSkip)
    val status = if (bytesToSkip == 0) HttpURLConnection.HTTP_OK else HttpURLConnection.HTTP_PARTIAL
    val headers = Map(
      "Access-Control-Allow-Headers" -> "range, accept-encoding",
      "Access-Control-Allow-Origin" -> "*",
      "Accept-Ranges" -> "bytes",
      "Connection" -> "close",
      "Content-Range" -> s"bytes $bytesToSkip-${file.size}/${file.size}",
    )
    StreamResult(
      status = status,
      headers = headers,
      mimeType = mimeType,
      inputStream = fis,
      contentLength = file.size - bytesToSkip,
    )
  }
}
