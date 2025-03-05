package backend.search

import java.net.URLDecoder

import com.google.inject.Inject
import formatter.ControllerAlbumDirJsonifier
import models.ModelJsonable.{ArtistDirJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}

import common.json.ToJsonableOps._

class SearchFormatter @Inject() (
    state: SearchState,
    albumJsonifier: ControllerAlbumDirJsonifier,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  import albumJsonifier.albumDirJsonable
  def search(path: String): Future[JsObject] = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    state.search(terms).map { case (songs, albums, artists) =>
      Json.obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
    }
  }
}
