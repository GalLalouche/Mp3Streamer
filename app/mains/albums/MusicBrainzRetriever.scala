package mains.albums

import java.io.File
import java.text.SimpleDateFormat
import java.util.concurrent.TimeoutException
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import common.CompositeDateFormat
import common.rich.RichT.richT
import common.rich.path.RichFile.richFile
import models.Album
import play.api.libs.json.{ JsArray, JsValue }
import play.api.libs.ws.WS

private object MusicBrainzRetriever extends MetadataRetriever {
	private val reconRepository: Map[String, String] =
		new File(getClass().getResource("musicbrainz-recons").getFile())
			.lines
			.map(_.split('=').mapTo(e => e(0) -> e(1)))
			.toMap

	private val sf = CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy")
	private val simplerSf = new SimpleDateFormat("yyyy-MM")
	override protected def jsonToAlbum(artist: String, js: JsValue): Option[Album] = {
		val dateString = js \ "first-release-date" asString;
		try {
			val date = sf.parse(dateString)
			if (date.getMillis > System.currentTimeMillis)
				None // album isn't out yet, trolls :\
			else
				Some(Album(artist, date.getYear, js \ "title" asString))
		} catch {
			case e: Exception => println("Failed to parse js " + js); throw e
		}
	}
	private val primaryTypes = Set("Album", "EP", "Live")
	override protected def getAlbumsJson(artistName: String): JsArray = {
		val artistId = reconRepository.get(artistName.toLowerCase).getOrElse {
			val $ = (getJson("artist/", "query" -> artistName) \ "artists").asJsArray.value
				.filter(_ has "type")
				.head
			if ("100" != ($ \ "score").asString)
				throw new NoSuchElementException("failed to get 100 match for artist " + artistName)
			$ \ "id" asString
		}
		val $ = try {
			getJson("release-group",
				"artist" -> (artistId),
				"limit" -> "100") \ "release-groups" asJsArray
		} catch {
			case e: Exception => println("Failed to get artist with id = " + artistId); throw e
		}
		new JsArray($.value
			.filter(_ has "first-release-date")
			.filter(_ has "primary-type")
			.filter(e => primaryTypes.contains(e \ "primary-type" asString))
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
			val result = Await.result(f, 30 seconds).json
			result
		} catch {
			case e: TimeoutException => println("Timed out in retrieving data for " + other.toSeq); throw e
		}
	}

	def main(args: Array[String]) {
		println(getAlbums("amaranthe").toList mkString "\n")
	}
}
