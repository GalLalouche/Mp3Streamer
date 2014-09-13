package other

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import common.RichString.richString
import models.Album
import play.api.libs.json.{ JsArray, JsString, JsValue }
import play.api.libs.ws.WS

class MetadataRetriever(apiKey: String = "d74d6d3b7e8fbfa6b733d376c4d41e73") {
	private implicit def richJson(js: JsValue) = new {
		def asString: String = js.asInstanceOf[JsString].value
	}
	private def jsonToAlbum(js: JsValue): Album = {

		val artist = js \ "artist" \ "name" asString
		val albumName = js \ "name" asString
		val albumJson = getAlbumJson(artist, albumName) \ "album"
		Album(artist, (albumJson \ "releasedate" asString).trim.captureWith(".*?(\\d{4}).*".r).toInt, albumName)
	}
	def getAlbums(artist: String): Seq[Album] =
		try {
			getArtistJson(artist) \ "topalbums" \ "album" match {
				case a: JsArray => a.value.map(jsonToAlbum)
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
		val f = ws.get()
		val result = Await.result(f, 5 seconds).json
		result
	}

	private def getArtistJson(artist: String) = getJson("artist.gettopalbums", "artist" -> artist)
	private def getAlbumJson(artist: String, album: String) = getJson("album.getinfo", "artist" -> artist, "album" -> album)

}

// simple test, can't unit test because it wraps a third party API
object MetadataRetriever {
	def main(args: Array[String]) {
		val $ = new MetadataRetriever
		println($.getAlbums("in vain"))
	}
}