package http4s

import java.util.concurrent.Executors

import backend.external.ExternalHttpRoutes
import backend.lucky.LuckyHttpRoutes
import backend.lyrics.LyricsHttpRoutes
import backend.new_albums.NewAlbumHttpRoutes
import backend.recent.RecentHttpRoutes
import backend.scorer.ScoreHttpRoutes
import backend.search.{IndexHttpRoutes, SearchHttpRoutes}
import cats.data.OptionT
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s._
import com.google.inject.Guice
import controllers.{ApplicationHttpRoutes, AssetHttpRoutes, PostersHttpRoutes, StreamHttpRoutes}
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.ErrorHandling
import playlist.PlaylistHttpRoutes
import songs.SongHttpRoutes

import scala.concurrent.ExecutionContext

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val injector = Guice.createInjector(
      new controllers.Module,
      new ScalaModule {
        override def configure(): Unit = bind[ExecutionContext].toInstance(
          ExecutionContext.fromExecutorService(
            Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors * 2),
          ),
        )
      },
    )
    lazy val app = Router(
      "" -> injector.instance[ApplicationHttpRoutes].routes,
      "" -> injector.instance[AssetHttpRoutes].routes,
      "data" -> injector.instance[SongHttpRoutes].routes,
      "external" -> injector.instance[ExternalHttpRoutes].routes,
      "index" -> injector.instance[IndexHttpRoutes].routes,
      "lucky" -> injector.instance[LuckyHttpRoutes].routes,
      "lyrics" -> injector.instance[LyricsHttpRoutes].routes,
      "new_albums" -> injector.instance[NewAlbumHttpRoutes].routes,
      "playlist" -> injector.instance[PlaylistHttpRoutes].routes,
      "posters" -> injector.instance[PostersHttpRoutes].routes,
      "recent" -> injector.instance[RecentHttpRoutes].routes,
      "score" -> injector.instance[ScoreHttpRoutes].routes,
      "search" -> injector.instance[SearchHttpRoutes].routes,
      "stream" -> injector.instance[StreamHttpRoutes].routes,
    )

    lazy val withErrorHandling = ErrorHandling.Custom
      .recoverWith(app) { case t: Throwable =>
        OptionT.liftF(IO(t.printStackTrace()) >> InternalServerError(t.getMessage))
      }
      .orNotFound
    EmberServerBuilder
      .default[IO]
      .withPort(port"9001")
      .withHttpApp(withErrorHandling)
      .build
      .useForever
  }
}
