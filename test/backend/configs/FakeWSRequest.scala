package backend.configs

import backend.Url
import common.io.WSAliases._
import common.rich.RichT._
import common.rich.collections.RichMap._
import common.rich.collections.RichSeq._
import common.rich.func.MoreSeqInstances
import monocle.macros.GenLens
import play.api.libs.ws._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

private case class FakeWSRequest private(
    response: WSRequest => FakeWSResponse,
    u: Url,
    method: String = "GET",
    body: WSBody = EmptyBody,
    headers: Map[String, Seq[String]] = Map(),
    queryString: Map[String, Seq[String]] = Map(),
    calc: Option[WSSignatureCalculator] = None,
    auth: Option[(String, String, WSAuthScheme)] = None,
    followRedirects: Option[Boolean] = None,
    requestTimeout: Option[Int] = None,
    virtualHost: Option[String] = None,
    proxyServer: Option[WSProxyServer] = None) extends WSRequest with MoreSeqInstances {
  override val url: String = u.address
  override type Self = FakeWSRequest
  override type Response = FakeWSResponse

  override def sign(calc: WSSignatureCalculator) = ???
  override def withAuth(username: String, password: String, scheme: WSAuthScheme) = ???
  override def addHttpHeaders(hdrs: (String, String)*) =
    GenLens[FakeWSRequest](_.headers).modify(_.merge(hdrs.toMultiMap))(this)
  override def addQueryStringParameters(parameters: (String, String)*) = ???
  override def withFollowRedirects(follow: Boolean) =
    this.copy(followRedirects = Some(follow))
  override def withRequestTimeout(timeout: Duration) = ???
  override def withRequestFilter(filter: WSRequestFilter) = ???
  override def withVirtualHost(vh: String) = ???
  override def withProxyServer(proxyServer: WSProxyServer) = ???
  override def withMethod(method: String) = this.ensuring(method == "GET")
  override def execute(): Future[FakeWSResponse] = Future successful {
    try response(this)
    catch {
      case _: MatchError => throw new AssertionError(s"Invalid configuration, no response to <$u>")
    }
  }
  override def stream() = ???
  override def uri = ???
  override def contentType = ???
  override def cookies = ???
  override def withHttpHeaders(headers: (String, String)*) = ???
  override def withQueryStringParameters(parameters: (String, String)*) = ???
  override def withCookies(cookies: WSCookie*) = ???
  override def withBody[T](body: T)(implicit evidence$1: BodyWritable[T]) = ???
  override def get() = execute()
  override def patch[T](body: T)(implicit evidence$2: BodyWritable[T]) = ???
  override def post[T](body: T)(implicit evidence$3: BodyWritable[T]) = ???
  override def put[T](body: T)(implicit evidence$4: BodyWritable[T]) = ???
  override def delete() = ???
  override def head() = ???
  override def options() = ???
  override def execute(method: String) = ???
}

private object FakeWSRequest {
  def apply(f: WSRequest => FakeWSResponse)(url: Url): FakeWSRequest =
    FakeWSRequest(response = f, u = url)
  def apply(response: => FakeWSResponse)(url: Url): FakeWSRequest =
    FakeWSRequest(response = response.const, u = url)
}
