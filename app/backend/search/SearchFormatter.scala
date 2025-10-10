package backend.search

import java.net.URLDecoder

import com.google.inject.Inject
import formatter.ControllerAlbumDirJsonifier
import models.ModelJsonable
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future}

import common.json.ToJsonableOps._

class SearchFormatter @Inject() (
    state: SearchState,
    albumJsonifier: ControllerAlbumDirJsonifier,
    ec: ExecutionContext,
    mj: ModelJsonable,
) {
  import albumJsonifier.albumDirJsonable
  import mj.{artistDirJsonifier, songJsonifier}

  private implicit val iec: ExecutionContext = ec

  def search(path: String): Future[JsObject] = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").view.map(_.toLowerCase).toVector
    state.search(terms).map { case (songs, albums, artists) =>
      Json.obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
    }
  }
}
