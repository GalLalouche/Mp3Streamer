package mains.albums

import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

import common.rich.primitives.RichString.richString
import models.Album
import play.api.libs.json.{ JsArray, JsValue }
import play.api.libs.ws.WS

private class LastfmMetadataRetriever(apiKey: String = "d74d6d3b7e8fbfa6b733d376c4d41e73") extends MetadataRetriever {
	override protected def jsonToAlbum(_artist: String, js: JsValue): Option[Album] = {
		def getAlbumJson(artist: String, album: String) = getJson("album.getinfo", "artist" -> artist, "album" -> album)
		val artist = js \ "artist" \ "name" asString
		val albumName = js \ "name" asString
		val albumJson = getAlbumJson(artist, albumName) \ "album"
		assert(artist == _artist, s"Artist found in Json ($artist) is different than the input (${_artist})")
		Try(Album(artist, (albumJson \ "releasedate" asString).trim.captureWith(".*?(\\d{4}).*".r).toInt, albumName)).toOption
	}
	override protected def getAlbumsJson(artist: String) =
		getJson("artist.gettopalbums", "artist" -> artist) \ "topalbums" \ "album" match {
			case a: JsArray => a
			case _ => throw new AssertionError("received json wasn't a JsArray")
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
			case e: TimeoutException =>
				System.err.println("Failed to retrieve data for " + other.toSeq)
				throw e
		}
	}

}