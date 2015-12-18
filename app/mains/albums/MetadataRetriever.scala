package mains.albums

import models.Album
import play.api.libs.json._
import play.api.libs.ws.WS

/**
 * Retrieves metadata about a band from some API 
 */
trait MetadataRetriever {
	protected def getAlbumsJson(artist: String): JsArray

	protected def jsonToAlbum(artist: String, js: JsValue): Option[Album]

	protected implicit def richJson(js: JsValue) = new {
		def asString: String = js.asInstanceOf[JsString].value
		def asJsArray: JsArray = try
			js.asInstanceOf[JsArray]
		catch {
			case e: ClassCastException => println("js: " + js + " is not an JsonArray"); throw e
		}
		def has(str: String) = {
			val $ = js \ str
			false == ($ == JsNull || $.isInstanceOf[JsUndefined]) &&
				($.isInstanceOf[JsString] == false || $.asInstanceOf[JsString].value != "")
		}
	}
	
	/** Gets all albums for a given artist */
	def getAlbums(artist: String): Iterator[Album] =
		try getAlbumsJson(artist)
			.value
			.iterator
			.map(jsonToAlbum(artist, _))
			.collect { case Some(a) => a }
		catch {
			case e: NoSuchElementException => println(e.getMessage()); Iterator.empty
			case e: Exception =>
				println("Could not get data for artist: " + artist + ". trying again in 10 seconds");
				Thread sleep 10000
				println("Retrying artist: " + artist)
				getAlbums(artist)
		} finally {
			WS.resetClient // this is needed for the application to die
			Thread sleep 1000 // doesn't overload the server
		}
}
