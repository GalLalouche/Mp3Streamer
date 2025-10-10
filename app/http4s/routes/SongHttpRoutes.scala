package http4s.routes

import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, jsonEncoder, shouldEncodeMp3}
import org.http4s.{HttpRoutes, Request}
import org.http4s.dsl.io._
import songs.SongFormatter

import cats.data.Reader
import cats.effect.IO

/** Handles fetch requests of JSON information. */
private class SongHttpRoutes @Inject() ($ : SongFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "randomSong" / "mp3" => Ok(run(req, $.randomMp3Song()))
    case req @ GET -> Root / "randomSong" / "flac" => Ok(run(req, $.randomFlacSong()))
    case req @ GET -> Root / "randomSong" => Ok(run(req, $.randomSong()))
    case req @ GET -> "album" /: path => Ok(run(req, $.album(decodePath(path))))
    case req @ GET -> "disc" /: disc /: path => Ok(run(req, $.discNumber(decodePath(path), disc)))
    case req @ GET -> "song" /: path => Ok(run(req, $.song(decodePath(path))))
    case req @ GET -> "nextSong" /: path => Ok(run(req, $.nextSong(decodePath(path))))
  }

  private def run[A](req: Request[IO], reader: Reader[Boolean, A]): A =
    reader.run(shouldEncodeMp3(req))
}
