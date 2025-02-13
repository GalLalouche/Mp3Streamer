package http4s

import javax.inject.Inject

import cats.data.OptionT
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import com.google.inject.Guice
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.ErrorHandling

private class Main @Inject() (routes: HttpRoutes[IO]) {
  lazy val app: HttpApp[IO] =
    ErrorHandling.Custom
      .recoverWith(routes) { case t: Throwable =>
        OptionT.liftF(IO(t.printStackTrace()) >> InternalServerError(t.getMessage))
      }
      .orNotFound
}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val injector = Guice.createInjector(Http4sModule)
    EmberServerBuilder
      .default[IO]
      .withPort(port"9000")
      .withHttpApp(injector.instance[Main].app)
      .build
      .useForever
  }
}
