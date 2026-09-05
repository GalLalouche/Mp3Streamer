package http4s.routes

import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import songs.SongFormatter

import cats.effect.IO

/** Handles fetch requests of JSON information. */
private class SongHttpRoutes @Inject() ($ : SongFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "randomSong" / "mp3" => Ok($.randomMp3Song())
    case GET -> Root / "randomSong" / "flac" => Ok($.randomFlacSong())
    case GET -> Root / "randomSong" => Ok($.randomSong())
    case GET -> "album" /: path => Ok($.album(decodePath(path)))
    case GET -> "disc" /: disc /: path => Ok($.discNumber(decodePath(path), disc))
    case GET -> "song" /: path => Ok($.song(decodePath(path)))
    case GET -> "nextSong" /: path => Ok($.nextSong(decodePath(path)))
  }
}
