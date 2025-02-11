package backend.search

import java.net.URLDecoder
import javax.inject.Inject

import controllers.ControllerAlbumJsonifier
import models.ModelJsonable.{ArtistDirJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

import common.json.ToJsonableOps._

private class SearchFormatter @Inject() (
    state: SearchState,
    albumJsonifier: ControllerAlbumJsonifier,
) {
  import albumJsonifier.albumJsonable
  def search(path: String): JsObject = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = state.search(terms)
    Json.obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
  }
}
