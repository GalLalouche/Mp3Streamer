package http4s.routes

import backend.score.ScorerFormatter
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{decodePath, fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import cats.effect.IO

private class ScoreHttpRoutes @Inject() ($ : ScorerFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> path => Ok(fromFuture($.getScore(decodePath(path))))
    // PATCH? It's about as close as it gets I guess...
    case PATCH -> path => Ok(fromFuture($.openScoreFile(decodePath(path))))
    // Doesn't make a whole of sense from a REST standpoint, but it's easier than fiddling with
    // HTTP parameters combined with a suffix path.
    case PUT -> "song" /: score /: path =>
      fromFuture($.updateSongScore(decodePath(path), score)) >> NoContent()
    case PUT -> "album" /: score /: path =>
      fromFuture($.updateAlbumScore(decodePath(path), score)) >> NoContent()
    case PUT -> "artist" /: score /: path =>
      fromFuture($.updateArtistScore(decodePath(path), score)) >> NoContent()
  }
}
