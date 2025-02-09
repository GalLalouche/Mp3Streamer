package http4s

import java.io.File
import java.net.{URLDecoder, URLEncoder}

import cats.MonadThrow
import cats.effect.{Concurrent, IO}
import cats.implicits.toFunctorOps
import fs2.Chunk
import fs2.io.file.Files
import org.http4s.{EntityDecoder, EntityEncoder, MediaType, Request, Response, StaticFile, Uri}
import org.http4s.headers.{`Content-Type`, `User-Agent`, Referer}
import play.api.libs.json.{Json, JsValue}

import scala.concurrent.Future

import common.rich.RichT.richT

object Http4sUtils {
  def fromFuture[A](f: => Future[A]): IO[A] = IO.fromFuture(IO(f))
  def fromFutureIO[A](f: => Future[IO[A]]): IO[A] = IO.fromFuture(IO(f)).flatten

  def decode(s: String): String = URLDecoder.decode(s, "UTF-8")
  def decodePath(path: Uri.Path): String =
    // Paths in http4s are matched using case GET -> path, meaning they might contain a leading
    // e.g., as in some/action/some_path, the resulting "some_path" would actually be rendered as
    // "/some_path".
    decode(path.renderString).mapIf(s => s.startsWith("/")).to(_.tail)
  def encode(s: String): String = URLEncoder.encode(s, "UTF-8")

  // Adapted from https://kutt.it/VFkQP8. I'm not using that one though since its JSON version
  // apparently clashes with the one I'm currently using ¯\_(ツ)_/¯.
  implicit def jsonEncoder[F[_], J <: JsValue]: EntityEncoder[F, J] =
    EntityEncoder[F, Chunk[Byte]]
      .contramap[J](Chunk array _.toString.getBytes("UTF8"))
      .withContentType(`Content-Type`(MediaType.application.json))
  implicit def jsonDecoder[F[_]: Concurrent, J <: JsValue]: EntityDecoder[F, JsValue] =
    EntityDecoder.text[F].map(Json.parse)

  def parseJson[F[_]: MonadThrow: Concurrent, A](req: Request[F], f: JsValue => A): F[A] =
    req.as[JsValue].map(f)
  def parseJson[A](req: Request[IO], f: JsValue => Future[A]): IO[A] =
    req.as[JsValue].flatMap(js => fromFuture(f(js)))
  def parseJson[A](req: Request[IO], f: JsValue => IO[A])(implicit di: DummyImplicit): IO[A] =
    req.as[JsValue].flatMap(js => f(js))

  def parseText[F[_]: MonadThrow: Concurrent, A](req: Request[F], f: String => A): F[A] =
    req.as[String].map(f)
  def parseText[A](req: Request[IO], f: String => Future[A]): IO[A] =
    req.as[String].flatMap(s => fromFuture(f(s)))
  def sendFile[F[_]: Files: MonadThrow](req: Request[F])(file: File): F[Response[F]] =
    StaticFile
      .fromString(file.getAbsolutePath, Some(req))
      .getOrElseF(throw new AssertionError("Missing files should be handled by the formatter"))

  def shouldEncodeMp3[F[_]](request: Request[F]): Boolean =
    request.headers.get[`User-Agent`].exists(_.product.value.toLowerCase.contains("Chrome")) ||
      request.headers.get[Referer].exists(_.uri.renderString.endsWith(".mp3"))
}
