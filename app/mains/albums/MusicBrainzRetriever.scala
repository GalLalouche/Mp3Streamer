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
		new File(getClass.getResource("musicbrainz-recons").getFile)
			.lines
			.map(_.split('=').mapTo(e => e(0) -> e(1)))
			.toMap

	override protected def jsonToAlbum(artist: String, js: JsValue): Option[Album] = {
		val date = CompositeDateFormat("yyyy-MM-dd", "yyyy-MM", "yyyy")
			.parse(js \ "first-release-date" asString)
		if (date.getMillis > System.currentTimeMillis)
			None // album isn't out yet, trolls :\
		else
			Some(Album(artist, date.getYear, js \ "title" asString))
	}
	
	override protected def getAlbumsJson(artistName: String): JsArray = {
		val artistId = reconRepository.get(artistName.toLowerCase).getOrElse {
			val webSearchResult = (getJson("artist/", "query" -> artistName) \ "artists").asJsArray.value
				.filter(_ has "type")
				.head
			if ("100" != (webSearchResult \ "score").asString)
				throw new NoSuchElementException("failed to get 100 match for artist " + artistName)
			webSearchResult \ "id" asString
		}
		val $ = try {
			getJson("release-group",
				"artist" -> (artistId),
				"limit" -> "100") \ "release-groups" asJsArray
		} catch {
			case e: Exception => System.err.println("Failed to get artist with id = " + artistId); throw e
		} 
		new JsArray($.value
			.filter(_ has "first-release-date")
			.filter(_ has "primary-type")
			.filter(e => Set("Album", "EP", "Live").contains(e \ "primary-type" asString))
			.filter(e => (e \ "secondary-types").asJsArray.value.isEmpty)
			.toList
			.sortBy(_ \ "first-release-date" asString))
	}

	private def getJson(method: String, other: (String, String)*): JsValue = {
		val webServiceRequest = WS.url("http://musicbrainz.org/ws/2/" + method)
			.withQueryString("fmt" -> "json").withQueryString(other: _*)
			.withHeaders("User-Agent" -> "Metadata Retriever")
		val response = webServiceRequest.get
		try {
			Await.result(response, 30 seconds).json
		} catch {
			case e: TimeoutException => System.err.println("Timed out in retrieving data for " + other.toSeq); throw e
		}
	}

	def main(args: Array[String]) {
		System.out.println(getAlbums("sun o)))").toList mkString "\n")
	}
}
