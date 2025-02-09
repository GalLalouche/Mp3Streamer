package songs

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, jsonEncoder, ShouldEncodeMp3}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scalaz.Reader

/** Handles fetch requests of JSON information. */
class SongHttpRoutes @Inject() ($ : SongFormatter) {
  private def run[A](reader: Reader[Boolean, A]): A = reader.run(ShouldEncodeMp3)
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "randomSong" / "mp3" => Ok(run($.randomMp3Song()))
    case GET -> Root / "randomSong" / "flac" => Ok(run($.randomFlacSong()))
    case GET -> Root / "randomSong" => Ok(run($.randomSong()))
    case GET -> "albums" /: path => Ok(run($.album(decodePath(path))))
    case GET -> "discs" /: disc /: path => Ok(run($.discNumber(decodePath(path), disc)))
    // TODO shouldn't be plural
    case GET -> "song" /: path => Ok(run($.song(decodePath(path))))
    case GET -> "nextSong" /: path => Ok(run($.nextSong(decodePath(path))))
  }
}
