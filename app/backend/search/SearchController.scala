package backend.search

import java.net.URLDecoder

import common.Jsonable
import common.concurrency.Extra
import controllers.LegacyController
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import models.{Album, ModelJsonable}
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsString, JsValue, Json}
import play.api.mvc.Action

object SearchController extends LegacyController with Extra
    with Jsonable.ToJsonableOps {
  private var index: CompositeIndex = CompositeIndex.create

  override def apply() {
    index = CompositeIndex.create
    Logger info "Search engine has been updated"
  }
  private implicit object albumJsonifier extends Jsonable[Album] { // Adds disc number information
    override def jsonify(a: Album): JsObject = {
      val $ = ModelJsonable.AlbumJsonifier.jsonify(a)
      if (a.songs.forall(_.discNumber.isDefined)) // All songs need to have a disc number (ignores bonus disc only)
        $ + ("discNumbers" -> JsArray(a.songs.map(_.discNumber.get).distinct.map(JsString)))
      else
        $
    }
    override def parse(json: JsValue): Album = ModelJsonable.AlbumJsonifier.parse(json)
  }
  def search(path: String) = Action {
    val terms = URLDecoder.decode(path, "UTF-8") split " " map (_.toLowerCase)
    val (songs, albums, artists) = index search terms

    Ok(Json obj(("songs", songs.jsonify), ("albums", albums.jsonify), ("artists", artists.jsonify)))
  }
}
