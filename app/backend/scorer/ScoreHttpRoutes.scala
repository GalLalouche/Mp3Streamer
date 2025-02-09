package backend.scorer

import javax.inject.Inject

import cats.effect.IO
import http4s.Http4sUtils.{decodePath, fromFuture, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

class ScoreHttpRoutes @Inject() ($ : ScorerFormatter) {
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> path => Ok(fromFuture($.getScore(decodePath(path))))
    // Doesn't make a whole of sense from a REST stand point, but it's easier than fiddling with
    // HTTP parameters combined with a suffix path.
    case PUT -> "song" /: score /: path =>
      fromFuture($.updateSongScore(decodePath(path), score)) >> NoContent()
    case PUT -> "album" /: score /: path =>
      fromFuture($.updateAlbumScore(decodePath(path), score)) >> NoContent()
    case PUT -> "artist" /: score /: path =>
      fromFuture($.updateArtistScore(decodePath(path), score)) >> NoContent()
  }
}
