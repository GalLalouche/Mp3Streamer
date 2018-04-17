package backend.search

import java.net.URLDecoder

import common.concurrency.Extra
import common.json.{JsonableOverrider, OJsonable, ToJsonableOps}
import controllers.LegacyController
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import models.{Album, ModelJsonable}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Action

object SearchController extends LegacyController with Extra
    with ToJsonableOps {
  private var index: CompositeIndex = CompositeIndex.create

  override def apply() {
    index = CompositeIndex.create
    Logger info "Search engine has been updated"
  }
  private implicit val albumJsonableWithDiscNumber: OJsonable[Album] =
    JsonableOverrider.oJsonify[Album]((a, original) => {
      if (a.songs.forall(_.discNumber.isDefined)) // All songs need to have a disc number (ignores bonus disc only)
        original + ("discNumbers" -> a.songs.map(_.discNumber.get).distinct.jsonify)
      else
        original
    })(ModelJsonable.AlbumJsonifier)

  def search(path: String) = Action {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = index search terms
    Ok(Json obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify))
  }
}
