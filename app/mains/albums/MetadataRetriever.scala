package mains.albums

import common.rich.collections.RichIterator._
import play.api.libs.json._
import play.api.libs.ws.WS

/**
  * Retrieves metadata about a band from some API
  */
private trait MetadataRetriever {
	/** @throws NoSuchElementException if information about the artist couldn't be retrieved */
	protected def getAlbumsJson(artist: String): JsArray

	protected def jsonToAlbum(artist: String, js: JsValue): Option[Album]

	protected implicit class RichJson(js: JsValue) {
		def asString: String = js.asInstanceOf[JsString].value
		def asJsArray: JsArray = try
			js.asInstanceOf[JsArray]
		catch {
			case e: ClassCastException => System.err.println("js: " + js + " is not an JsonArray"); throw e
		}
		def has(str: String) = {
			val $ = js \ str
			false == ($ == JsNull || $.isInstanceOf[JsUndefined]) &&
				($.isInstanceOf[JsString] == false || $.asInstanceOf[JsString].value != "")
		}
	}

	/** Gets all albums for a given artist */
	def getAlbums(artist: String, tryNumber: Int = 0): Iterator[Album] =
		try
			getAlbumsJson(artist)
				.value
				.iterator
				.mapDefined(jsonToAlbum(artist, _))
		catch {
			case e: NoSuchElementException =>
				println(e.getMessage)
				Iterator.empty
			case e: Exception =>
				if (tryNumber < 5) {
					System.err.println("Could not get data for artist: " + artist + ". trying again in 10 seconds")
					Thread sleep 10000
					System.err.println("Retrying artist: " + artist)
					getAlbums(artist, tryNumber + 1)
				} else {
					System.err.println("Could not get data for artist: " + artist + ". giving up :(")
					Iterator.empty
				}
		} finally {
			WS.resetClient // this is needed for the application to die
			Thread sleep 1000 // doesn't overload the server
		}
}
