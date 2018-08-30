package backend.search

import java.net.URLDecoder

import backend.logging.Logger
import common.concurrency.Extra
import common.json.{JsonableOverrider, OJsonable, ToJsonableOps}
import controllers.LegacyController
import models.{Album, ModelJsonable}
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.Json
import play.api.mvc.Action

object SearchController extends LegacyController with Extra
    with ToJsonableOps {
  private def createCompositeIndex(): CompositeIndex = c.injector.instance[CompositeIndexFactory].create()
  private val logger = c.injector.instance[Logger]
  private var index: CompositeIndex = createCompositeIndex()

  override def apply(): Unit = {
    index = createCompositeIndex()
    logger info "Search engine has been updated"
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
