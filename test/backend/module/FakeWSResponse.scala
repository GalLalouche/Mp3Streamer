package backend.module

import java.net.URI

import akka.util.ByteString
import play.api.libs.ws.WSCookie

import common.io.WSAliases._

case class FakeWSResponse(
    allHeaders: Map[String, Seq[String]] = Map(),
    status: Int = -1,
    cookies: Seq[WSCookie] = Nil,
    bytes: Array[Byte] = Array(),
) extends WSResponse {
  override def underlying[T] = this.asInstanceOf[T]
  override def header(key: String) = allHeaders(key).headOption
  override def cookie(name: String) = cookies.find(_.name == name)
  override def statusText =
    throw new UnsupportedOperationException("If you do this then you are stupid")
  override def body = new String(bytes)
  override def bodyAsBytes = ByteString(bytes)
  override def headers = allHeaders
  override def bodyAsSource = ???
  override def uri: URI = ???
}
