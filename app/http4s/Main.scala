package http4s

import javax.inject.Inject

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
import org.http4s.HttpApp
import org.http4s.dsl.io._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.ErrorHandling
import playlist.PlaylistHttpRoutes
import songs.SongHttpRoutes

private class Main @Inject() (
    application: ApplicationHttpRoutes,
    asset: AssetHttpRoutes,
    song: SongHttpRoutes,
    external: ExternalHttpRoutes,
    index: IndexHttpRoutes,
    lucky: LuckyHttpRoutes,
    lyrics: LyricsHttpRoutes,
    newAlbum: NewAlbumHttpRoutes,
    playlist: PlaylistHttpRoutes,
    posters: PostersHttpRoutes,
    recent: RecentHttpRoutes,
    score: ScoreHttpRoutes,
    search: SearchHttpRoutes,
    stream: StreamHttpRoutes,
) {
  lazy val app: HttpApp[IO] = {
    val routes = Router(
      "" -> application.routes,
      "" -> asset.routes,
      "data" -> song.routes,
      "external" -> external.routes,
      "index" -> index.routes,
      "lucky" -> lucky.routes,
      "lyrics" -> lyrics.routes,
      "new_albums" -> newAlbum.routes,
      "playlist" -> playlist.routes,
      "posters" -> posters.routes,
      "recent" -> recent.routes,
      "score" -> score.routes,
      "search" -> search.routes,
      "stream" -> stream.routes,
    )

    ErrorHandling.Custom
      .recoverWith(routes) { case t: Throwable =>
        OptionT.liftF(IO(t.printStackTrace()) >> InternalServerError(t.getMessage))
      }
      .orNotFound
  }
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
