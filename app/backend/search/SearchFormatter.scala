package backend.search

import java.net.URLDecoder

import javax.inject.Inject
import models.{Album, ModelJsonable}
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

import scalaz.std.OptionInstances
import scalaz.syntax.ToTraverseOps
import common.rich.func.MoreTraverseInstances

import common.json.{JsonableOverrider, OJsonable}
import common.RichJson._
import common.json.ToJsonableOps._

private class SearchFormatter @Inject()(state: SearchState) {
  private implicit val albumJsonableWithDiscNumber: OJsonable[Album] =
    JsonableOverrider.oJsonify[Album]((a, original) => original
        .append("discNumbers", SearchFormatter.discNumbers(a))
        .append("composer" -> a.composer)
        .append("conductor" -> a.conductor)
        .append("opus" -> a.opus)
        .append("orchestra" -> a.orchestra)
        .append("performanceYear" -> a.performanceYear)
    )(ModelJsonable.AlbumJsonifier)

  def search(path: String): JsObject = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = state search terms
    Json obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
  }
}

object SearchFormatter extends ToTraverseOps with MoreTraverseInstances with OptionInstances {
  // All songs need to have a disc number (ignores bonus disc only).
  private def discNumbers(a: Album): Option[Seq[String]] =
    a.songs.traverse(s => s.discNumber.map(_ -> s.track))
        // Sort disc numbers by track order.
        .map(_.sortBy(_._2).map(_._1).distinct)
}
