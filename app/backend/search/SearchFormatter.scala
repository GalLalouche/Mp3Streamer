package backend.search

import java.net.URLDecoder
import javax.inject.Inject

import backend.search.SearchFormatter._
import controllers.UrlPathUtils
import models.{Album, ModelJsonable}
import models.ModelJsonable.{ArtistJsonifier, SongJsonifier}
import play.api.libs.json.{JsObject, Json}

import common.rich.func.MoreTraverseInstances._
import scalaz.std.option.optionInstance
import scalaz.syntax.traverse.ToTraverseOps

import common.json.{JsonableOverrider, OJsonable}
import common.json.RichJson._
import common.json.ToJsonableOps._

private class SearchFormatter @Inject() (state: SearchState, urlPathUtils: UrlPathUtils) {
  private implicit val albumJsonableWithExtraInfo: OJsonable[Album] =
    JsonableOverrider.oJsonify[Album]((a, original) =>
      original
        .append("discNumbers" -> discNumbers(a))
        .append("composer" -> a.composer)
        .append("conductor" -> a.conductor)
        .append("opus" -> a.opus)
        .append("orchestra" -> a.orchestra)
        .append("performanceYear" -> a.performanceYear)
        .append("dir" -> Some(urlPathUtils.encodePath(a.dir))),
    )(ModelJsonable.AlbumJsonifier)
  def search(path: String): JsObject = {
    val terms = URLDecoder.decode(path, "UTF-8").split(" ").map(_.toLowerCase)
    val (songs, albums, artists) = state.search(terms)
    Json.obj("songs" -> songs.jsonify, "albums" -> albums.jsonify, "artists" -> artists.jsonify)
  }
}

private object SearchFormatter {
  // If not all songs have a disc number, returns None (i.e., ignores albums with bonus disc only).
  private def discNumbers(a: Album): Option[Seq[String]] =
    a.songs
      .traverse(s => s.discNumber.map(_ -> s.trackNumber))
      // Sort disc numbers by track order.
      .map(_.sortBy(_._2).map(_._1).distinct)
}
