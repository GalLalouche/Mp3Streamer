package http4s.routes

import backend.new_albums.NewAlbumsFormatter
import cats.effect.IO
import com.google.inject.Inject
import http4s.routes.Http4sUtils.{fromFuture, fromFutureIO, jsonEncoder}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

import scala.concurrent.ExecutionContext

import common.rich.func.ToMoreFoldableOps.toMoreFoldableOps
import scalaz.std.option.optionInstance

/**
 * A web interface to new albums finder. Displays new albums and can update the current file /
 * ignoring policy.
 */
private class NewAlbumHttpRoutes @Inject() ($ : NewAlbumsFormatter, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // FIXME fix this
    case GET -> Root / "index.html" => ???
    case GET -> Root / "albums" => Ok(fromFuture($.albums))
    case GET -> Root / "albums" / artist =>
      val notFound = NotFound(s"Artist <$artist> is not reconciled")
      fromFutureIO($.forArtist(artist).map(_.mapHeadOrElse(Ok(_), notFound)))
    case PUT -> Root / "artist" / "remove" / artist =>
      fromFuture($.removeArtist(artist)) *> NoContent()
    case PUT -> Root / "artist" / "ignore" / artist =>
      fromFuture($.ignoreArtist(artist)) *> NoContent()
    case PUT -> Root / "artist" / "unignore" / artist =>
      fromFuture($.unignoreArtist(artist)) *> NoContent()
    case req @ PUT -> Root / "album" / "remove" / albumReconId =>
      fromFuture($.removeAlbum(albumReconId)) *> NoContent()
    case req @ PUT -> Root / "album" / "ignore" / albumReconId =>
      fromFuture($.ignoreAlbum(albumReconId)) *> NoContent()
  }
}
