package other

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import common.RichString.richString
import models.Album
import play.api.libs.json.{ JsArray, JsString, JsValue }
import play.api.libs.ws.WS
import java.util.concurrent.TimeoutException
import scala.util.Try

class LastfmMetadataRetriever(apiKey: String = "d74d6d3b7e8fbfa6b733d376c4d41e73") extends MetadataRetriever {
	private implicit def richJson(js: JsValue) = new {
		def asString: String = js.asInstanceOf[JsString].value
	}
	private def jsonToAlbum(js: JsValue): Option[Album] = {
		val artist = js \ "artist" \ "name" asString
		val albumName = js \ "name" asString
		val albumJson = getAlbumJson(artist, albumName) \ "album"
		Try(Album(artist, (albumJson \ "releasedate" asString).trim.captureWith(".*?(\\d{4}).*".r).toInt, albumName)).toOption
	}
	override def getAlbums(artist: String): Iterator[Album] =
		try {
			getArtistJson(artist) \ "topalbums" \ "album" match {
				case a: JsArray => a.value.iterator.map(jsonToAlbum).filter(_.isDefined).map(_.get)
				case _ => throw new MatchError // removes warning
			}
		} finally {
			WS.resetClient // this is needed for the application to die
		}

	private def getJson(method: String, other: (String, String)*): JsValue = {
		val ws = WS.url("http://ws.audioscrobbler.com/2.0/")
			.withQueryString(
				"method" -> method,
				"api_key" -> apiKey,
				"format" -> "json"
			).withQueryString(other: _*)
			.withHeaders("User-Agent" -> "Metadata Retriever")
		val f = ws.get()
		try {
			val result = Await.result(f, 30 seconds).json
			result
		} catch {
			case e: TimeoutException => println("Failed to retrieve data for " + other.toSeq); throw e
		}
	}

	private def getArtistJson(artist: String) = getJson("artist.gettopalbums", "artist" -> artist)
	private def getAlbumJson(artist: String, album: String) = getJson("album.getinfo", "artist" -> artist, "album" -> album)

}

// simple test, can't unit test because it wraps a third party API
object LastfmMetadataRetriever {
	def main(args: Array[String]) {
		val $ = new LastfmMetadataRetriever
		println($.getAlbums("in vain"))
	}
}