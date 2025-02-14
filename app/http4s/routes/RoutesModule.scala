package http4s.routes

import cats.effect.IO
import com.google.inject.{Provides, Singleton}
import formatter.{UrlDecoder, UrlEncoder}
import net.codingwell.scalaguice.ScalaModule
import org.http4s.HttpRoutes
import org.http4s.server.Router

private[http4s] object RoutesModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UrlEncoder].toInstance(Http4sUtils.encode)
    bind[UrlDecoder].toInstance(Http4sUtils.decode)
  }

  @Provides @Singleton private def provideRoutes(
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
  ): HttpRoutes[IO] = Router(
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
}
