package other

import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try
import common.RichString.richString
import models.Album
import play.api.libs.json._
import play.api.libs.ws.WS
import java.text.SimpleDateFormat
import common.RichString._
import common.Jsoner._
class MusicBrainzRetriever extends MetadataRetriever {
	val sf = new SimpleDateFormat("yyyy-MM-dd")
	override protected def jsonToAlbum(artist: String, js: JsValue): Option[Album] = {
		val albumName = js \ "title" asString;
		val year = (js \ "first-release-date" asString).captureWith(".*(\\d{4}).*".r).toInt
		Some(Album(artist, year, albumName))
	}
	override protected def getAlbumsJson(artistName: String) = {
		val artist = (getJson("artist/", "query" -> artistName) \ "artist").asInstanceOf[JsArray].value
			.filter(_ has "type")
			.head
		assert("100" == (artist \ "score").asString, "could not find a certain match for " + artistName)
		val $ = getJson("release-group",
			"artist" -> (artist \ "id" asString),
			"limit" -> "100") \ "release-groups" asJsArray;
		new JsArray($.value
			.filter(e => (e \ "secondary-types").asJsArray.value.isEmpty)
			.toList
			.sortBy(_ \ "first-release-date" asString))
	}

	private def getJson(method: String, other: (String, String)*): JsValue = {
		val ws = WS.url("http://musicbrainz.org/ws/2/" + method)
			.withQueryString("fmt" -> "json").withQueryString(other: _*)
			.withHeaders("User-Agent" -> "Metadata Retriever")
		val f = ws.get()
		try {
			val result = Await.result(f, 10 seconds).json
			result
		} catch {
			case e: TimeoutException => println("Failed to retrieve data for " + other.toSeq); throw e
		}
	}

}

// simple test, can't unit test because it wraps a third party API
object MusicBrainzRetriever {
	def main(args: Array[String]) {
		val $ = new MusicBrainzRetriever
		println($.getAlbums("finntroll").toList mkString "\n")
	}
}