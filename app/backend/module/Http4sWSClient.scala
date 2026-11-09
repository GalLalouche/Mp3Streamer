package backend.module

import java.net.URI

import backend.module.Http4sWSClient.{Http4sWSRequest, Http4sWSResponse}
import io.lemonlabs.uri.Url
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.http4s.{AuthScheme, BasicCredentials, Credentials, Header, Headers, Method, Request, RequestCookie, Response, ResponseCookie, Status, Uri}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.{`Set-Cookie`, Authorization, Cookie, Host}
import play.api.libs.ws._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import common.rich.func.kats.ToMoreEmptyOps.{emptyFilter, emptyOption}

import common.io.WSAliases._
import common.rich.RichT.richT
import common.rich.collections.RichMap.{richSemigroupMap, _}
import common.rich.collections.RichSeq._
import common.rich.primitives.RichEither.richEither

/**
 * A [[WSClient]] backed by http4s's ember client. AsyncHttpClient's HTTP/1.1 + Netty TLS
 * fingerprint is rejected with 403 by Cloudflare-protected hosts such as genius.com, while ember's
 * fingerprint is accepted, so the HTTP layer is swapped without touching the WS API the rest of the
 * code uses.
 */
private class Http4sWSClient(runtime: IORuntime) extends WSClient {
  private val clientResource =
    EmberClientBuilder.default[IO].withMaxResponseHeaderSize(1 << 14).build

  override def underlying[T]: T = this.asInstanceOf[T]
  override def url(url: String): WSRequest = Http4sWSRequest(this, Url.parse(url))
  def execute(req: Http4sWSRequest): Future[WSResponse] =
    clientResource.use(_.run(req.toHttp4s).use(toResponse(req.uri))).unsafeToFuture()(runtime)

  private def toResponse(uri: URI)(response: Response[IO]): IO[Http4sWSResponse] =
    response.body.compile.to(Array).map(new Http4sWSResponse(response, _, uri))
  override def close(): Unit = ()
}

private object Http4sWSClient {
  def apply(runtime: IORuntime): WSClient = new Http4sWSClient(runtime)

  private case class Http4sWSRequest(
      client: Http4sWSClient,
      u: Url,
      method: String = "GET",
      body: WSBody = EmptyBody,
      headers: Map[String, Seq[String]] = Map(),
      queryString: Map[String, Seq[String]] = Map(),
      cookies: Seq[WSCookie] = Nil,
      calc: Option[WSSignatureCalculator] = None,
      auth: Option[(String, String, WSAuthScheme)] = None,
      followRedirects: Option[Boolean] = None,
      requestTimeout: Option[Duration] = None,
      virtualHost: Option[String] = None,
      proxyServer: Option[WSProxyServer] = None,
      disableUrlEncoding: Option[Boolean] = None,
  ) extends WSRequest {
    override type Self = Http4sWSRequest
    override type Response = WSResponse

    override val url: String = u.toStringPunycode
    override lazy val uri: URI = URI.create(url)

    override def contentType: Option[String] = headers.get("Content-Type").flatMap(_.headOption)

    override def sign(calc: WSSignatureCalculator): Self = copy(calc = Some(calc))
    override def withAuth(username: String, password: String, scheme: WSAuthScheme): Self =
      copy(auth = Some((username, password, scheme)))
    override def withHttpHeaders(hdrs: (String, String)*): Self = copy(headers = hdrs.toMultiMap)
    override def addQueryStringParameters(parameters: (String, String)*): Self =
      copy(queryString = parameters.foldLeft(queryString) { case (map, (key, value)) =>
        map.upsert(key, Vector(value))
      })
    override def withQueryStringParameters(parameters: (String, String)*): Self =
      copy(queryString = parameters.toMultiMap)
    override def withCookies(cookies: WSCookie*): Self = copy(cookies = cookies)
    override def withFollowRedirects(follow: Boolean): Self = copy(followRedirects = Some(follow))
    override def withDisableUrlEncoding(disableUrlEncoding: Boolean): Self =
      copy(disableUrlEncoding = Some(disableUrlEncoding))
    override def withRequestTimeout(timeout: Duration): Self = copy(requestTimeout = Some(timeout))
    override def withRequestFilter(filter: WSRequestFilter): Self = unsupported("withRequestFilter")
    override def withVirtualHost(vh: String): Self = copy(virtualHost = Some(vh))
    override def withProxyServer(proxyServer: WSProxyServer): Self = unsupported("withProxyServer")
    override def withUrl(newUrl: String): Self = copy(u = Url.parse(newUrl))
    override def withMethod(method: String): Self = copy(method = method)
    override def withBody[T: BodyWritable](body: T): Self =
      copy(body = implicitly[BodyWritable[T]].transform(body))

    override def get(): Future[Response] = execute("GET")
    override def patch[T: BodyWritable](body: T): Future[Response] = withBody(body).execute("PATCH")
    override def post[T: BodyWritable](body: T): Future[Response] = withBody(body).execute("POST")
    override def put[T: BodyWritable](body: T): Future[Response] = withBody(body).execute("PUT")
    override def delete(): Future[Response] = execute("DELETE")
    override def head(): Future[Response] = execute("HEAD")
    override def options(): Future[Response] = execute("OPTIONS")
    override def execute(method: String): Future[Response] = withMethod(method).execute()
    override def execute(): Future[Response] = client.execute(this)
    override def stream(): Future[Response] = unsupported("stream")

    private def unsupported(what: String) =
      throw new UnsupportedOperationException(s"Http4sWSClient does not support $what")

    def toHttp4s: Request[IO] = Request[IO](
      method = Method.fromString(method).getOrElse(Method.GET),
      uri = Uri.unsafeFromString(url).withMultiValueQueryParams(queryString),
    )
      .withHeaders(Headers(rawHeaders))
      .withBodyStream(body match {
        case EmptyBody => fs2.Stream.empty
        case InMemoryBody(bytes) => fs2.Stream.emits(bytes.toArray)
        case SourceBody(_) => unsupported("streaming request bodies")
      })

    private def rawHeaders: Seq[Header.Raw] = {
      val raw: Seq[Header.Raw] = headers.view.flatMap { case (name, values) =>
        values.map(value => Header.Raw(org.typelevel.ci.CIString(name), value))
      }.toVector
      val cookieHeader =
        NonEmptyList
          .fromList(cookies.view.map(c => RequestCookie(c.name, c.value)).toList)
          .map(Cookie(_))
      val hostHeader = virtualHost.map(Host(_))
      val authHeader = auth.map { case (username, password, scheme) =>
        Authorization(scheme match {
          case WSAuthScheme.BASIC =>
            Credentials.Token(AuthScheme.Basic, BasicCredentials(username, password).token)
        })
      }
      def values[A](h: Option[A])(implicit ev: A => Header.ToRaw): Seq[Header.Raw] =
        h.map(_.values).getOrElse(Nil)
      raw ++ values(hostHeader) ++ values(cookieHeader) ++ values(authHeader)
    }
  }

  private class Http4sWSResponse(response: Response[IO], bytes: Array[Byte], override val uri: URI)
      extends WSResponse {
    override def underlying[T]: T = this.asInstanceOf[T]
    override def status: Int = response.status.code
    override def statusText: String =
      Status.fromInt(response.status.code).map(_.reason).getOrEmpty
    override def headers: Map[String, Seq[String]] =
      response.headers.headers.groupBy(_.name.toString).properMapValues(_.map(_.value))
    override def body: String = new String(bytes, "UTF-8")
    override def bodyAsBytes: ByteString = ByteString(bytes)
    override def bodyAsSource: Source[ByteString, _] =
      throw new UnsupportedOperationException("Http4sWSClient does not support streaming responses")
    override def cookies: Seq[WSCookie] = response.headers.headers.flatMap { raw =>
      `Set-Cookie`
        .parse(raw.value)
        .map(_.cookie |> toWSCookie)
        .toOption
        .emptyFilter(raw.name == `Set-Cookie`.name)
    }
    override def cookie(name: String): Option[WSCookie] = cookies.find(_.name == name)
  }

  private[this] def toWSCookie(cookie: ResponseCookie): WSCookie = DefaultWSCookie(
    name = cookie.name,
    value = cookie.content,
    domain = cookie.domain,
    path = cookie.path,
    maxAge = cookie.maxAge,
    secure = cookie.secure,
    httpOnly = cookie.httpOnly,
  )
}
