package http4s

import com.comcast.ip4s._
import com.google.inject.{Guice, Inject, Provider}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.ErrorHandling

import scala.concurrent.duration.Duration

import cats.data.OptionT
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.effect.unsafe.IORuntimeConfig

private class Main @Inject() (routes: Provider[HttpRoutes[IO]]) {
  lazy val app: HttpApp[IO] =
    ErrorHandling.Custom
      .recoverWith(routes.get()) { case t: Throwable =>
        OptionT.liftF(IO(t.printStackTrace()) >> InternalServerError(t.getMessage))
      }
      .orNotFound
}

private object Main extends IOApp {
  protected override def runtimeConfig: IORuntimeConfig =
    super.runtimeConfig.copy(cpuStarvationCheckThreshold = 1)

  override def run(args: List[String]): IO[ExitCode] = {
    val injector = Guice.createInjector(Http4sModule)
    run(injector.instance[Main], port"9000").useForever
  }

  def run(main: Main, port: Port): Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withPort(port)
      .withHttpApp(main.app)
      .withShutdownTimeout(Duration.Zero)
      .build
}
