package backend.configs
import backend.Url
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

private case class FakeWSRequest(
    response: WSResponse,
    u: Url,
    method: String = "GET",
    body: WSBody = EmptyBody,
    headers: Map[String, List[String]] = Map(),
    queryString: Map[String, Seq[String]] = Map(),
    calc: Option[WSSignatureCalculator] = None,
    auth: Option[(String, String, WSAuthScheme)] = None,
    followRedirects: Option[Boolean] = None,
    requestTimeout: Option[Int] = None,
    virtualHost: Option[String] = None,
    proxyServer: Option[WSProxyServer] = None) extends WSRequest {
  override val url: String = u.address

  override def sign(calc: WSSignatureCalculator): WSRequest = ???
  override def withAuth(username: String, password: String, scheme: WSAuthScheme): WSRequest = ???
  override def withHeaders(hdrs: (String, String)*): WSRequest = copy(headers =
      hdrs.foldLeft(headers)((map, e) => map.updated(e._1, e._2 :: map.getOrElse(e._1, Nil))))
  override def withQueryString(parameters: (String, String)*): WSRequest = ???
  override def withFollowRedirects(follow: Boolean): WSRequest =
    this.copy(followRedirects = Some(follow))
  override def withRequestTimeout(timeout: Duration): WSRequest = ???
  override def withRequestFilter(filter: WSRequestFilter): WSRequest = ???
  override def withVirtualHost(vh: String): WSRequest = ???
  override def withProxyServer(proxyServer: WSProxyServer): WSRequest = ???
  override def withBody(body: WSBody): WSRequest = ???
  override def withMethod(method: String): WSRequest = this.ensuring(method == "GET")
  override def execute(): Future[WSResponse] = Future successful response
  override def stream(): Future[StreamedResponse] = ???
  override def streamWithEnumerator(): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = ???
}
