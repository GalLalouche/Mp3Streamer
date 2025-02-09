package backend.lyrics

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, fromFuture, parseText}
import io.lemonlabs.uri.Url
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class LyricsHttpRoutes @Inject() ($ : LyricsFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> path => Ok(fromFuture($.get(decodePath(path))))
    case req @ POST -> "push" /: path =>
      Ok(parseText(req, s => $.push(decodePath(path), Url.parse(s))))
    case POST -> "instrumental" /: "song" /: path =>
      Ok(fromFuture($.setInstrumentalSong(decodePath(path))))
    case POST -> "instrumental" /: "artist" /: path =>
      Ok(fromFuture($.setInstrumentalArtist(decodePath(path))))
  }
}
