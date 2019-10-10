package backend.search

import java.net.URLDecoder

import javax.inject.Inject
import models.{Album, ModelJsonable}
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

import scalaz.syntax.traverse.ToTraverseOps
import common.rich.func.MoreTraverseInstances._

import common.json.{JsonableOverrider, OJsonable}
import common.RichJson._
import common.json.ToJsonableOps._
import SearchFormatter._
import com.google.common.annotations.VisibleForTesting

private class SearchFormatter @Inject()(state: SearchState) {
  def search(path: String): JsObject = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = state search terms
    Json obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
  }
}

private object SearchFormatter {
  implicit val albumJsonableWithDiscNumber: OJsonable[Album] =
    JsonableOverrider.oJsonify[Album]((a, original) => original
        .append("discNumbers" -> discNumbers(a))
        .append("composer" -> a.composer)
        .append("conductor" -> a.conductor)
        .append("opus" -> a.opus)
        .append("orchestra" -> a.orchestra)
        .append("performanceYear" -> a.performanceYear)
    )(ModelJsonable.AlbumJsonifier)

  // If not all songs have a disc number, returns None (i.e., ignores albums with bonus disc only).
  private def discNumbers(a: Album): Option[Seq[String]] =
    a.songs.traverse(s => s.discNumber.map(_ -> s.track))
        // Sort disc numbers by track order.
        .map(_.sortBy(_._2).map(_._1).distinct)
}
