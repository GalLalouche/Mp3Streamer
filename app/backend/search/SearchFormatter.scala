package backend.search

import java.net.URLDecoder

import common.json.{JsonableOverrider, OJsonable, ToJsonableOps}
import common.rich.RichT._
import javax.inject.Inject
import models.{Album, ModelJsonable}
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

private class SearchFormatter @Inject()(state: SearchState) extends ToJsonableOps {
  private implicit val albumJsonableWithDiscNumber: OJsonable[Album] =
    JsonableOverrider.oJsonify[Album]((a, original) => {
      original.mapIf(a.songs.forall(_.discNumber.isDefined)) // All songs need to have a disc number (ignores bonus disc only)
          .to(_ + ("discNumbers" -> a.songs.map(_.discNumber.get).distinct.sorted.jsonify))
    })(ModelJsonable.AlbumJsonifier)

  def search(path: String): JsObject = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = state search terms
    Json obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
  }
}
