package controllers

import java.io.InputStream

case class StreamResult(
    status: Int,
    headers: Map[String, String],
    mimeType: String,
    inputStream: InputStream,
    contentLength: Long,
) {
  def withHeaders(first: (String, String), rest: (String, String)*): StreamResult =
    copy(headers = headers ++ (first +: rest))
}
