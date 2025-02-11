package http4s

import java.net.URLEncoder

import backend.module.TestModuleConfiguration
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.google.inject.{Guice, Injector, Module, Provides}
import http4s.Http4sUtils.{jsonDecoder, jsonEncoder}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.http4s.{EntityDecoder, EntityEncoder, Method, Request, Uri}
import org.http4s.client.Client
import org.scalatest.Suite
import play.api.libs.json.JsValue

import common.io.{BaseDirectory, DirectoryRef, RootDirectory}
import common.test.AuxSpecs

trait Http4sSpecs extends AuxSpecs { self: Suite =>
  protected def module: Module = new ScalaModule {
    override def configure(): Unit =
      install(TestModuleConfiguration().module)
    @Provides @BaseDirectory private def provideBaseDir(
        @RootDirectory rootDir: DirectoryRef,
    ): DirectoryRef = rootDir
  }
  protected final lazy val injector: Injector = Guice.createInjector(module)
  private lazy val app = injector.instance[Main].app
  def encode(s: String): String = URLEncoder.encode(s, "UTF-8")
  def encodeUri(s: String): Uri = Uri.unsafeFromString(encode(s))

  def makeRequest[A: EntityDecoder[IO, *]](request: Request[IO]): IO[A] =
    Client.fromHttpApp(app).expect[A](request)

  /** Blocking. */
  def get[A: EntityDecoder[IO, *]](path: String): A =
    makeRequest[A](Request[IO](method = Method.GET, uri = Uri.unsafeFromString(path)))
      .unsafeRunSync()
  def getBytes(path: String): Array[Byte] = get[Array[Byte]](path)
  def getJson[J <: JsValue](path: String): J = get[JsValue](path).asInstanceOf[J]

  def post[Body: EntityEncoder[IO, *], Result: EntityDecoder[IO, *]](
      path: String,
      body: Body,
  ): IO[Result] = withBody[Body, Result](Method.POST, path, body)
  def post[Result: EntityDecoder[IO, *]](path: String): IO[Result] =
    makeRequest[Result](Request[IO](method = Method.POST, uri = Uri.unsafeFromString(path)))

  def put[Body: EntityEncoder[IO, *], Result: EntityDecoder[IO, *]](
      path: String,
      body: Body,
  ): IO[Result] = withBody[Body, Result](Method.PUT, path, body)
  def putJson[Result: EntityDecoder[IO, *]](path: String, body: JsValue): IO[Result] =
    put[JsValue, Result](path, body)

  private def withBody[Body: EntityEncoder[IO, *], Result: EntityDecoder[IO, *]](
      method: Method,
      path: String,
      body: Body,
  ): IO[Result] = makeRequest[Result](
    Request[IO](
      method = method,
      uri = Uri.unsafeFromString(path),
      body = implicitly[EntityEncoder[IO, Body]].toEntity(body).body,
    ),
  )

  def delete[Result: EntityDecoder[IO, *]](path: String): IO[Result] =
    makeRequest[Result](Request(method = Method.DELETE, uri = Uri.unsafeFromString(path)))
}
