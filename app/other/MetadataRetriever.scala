package other

import models.Album
import play.api.libs.json._
import play.api.libs.ws.WS

trait MetadataRetriever {
	protected def getAlbumsJson(artist: String): JsArray

	protected def jsonToAlbum(artist: String, js: JsValue): Option[Album]

	protected implicit def richJson(js: JsValue) = new {
		def asString: String = js.asInstanceOf[JsString].value
		def asJsArray: JsArray = js.asInstanceOf[JsArray]
		def has(str: String) = {
			val $ = js \ str
			false == ($ == JsNull || $.isInstanceOf[JsUndefined]) &&
				($.isInstanceOf[JsString] == false || $.asInstanceOf[JsString].value != "")
		}
	}
	def getAlbums(artist: String): Iterator[Album] =
		try getAlbumsJson(artist)
			.value
			.iterator
			.map(jsonToAlbum(artist, _))
			.collect { case Some(a) => a }
		catch { case e: Exception => println("Could not get data for artist: " + artist); Thread sleep 10000; Iterator.empty }
		finally WS.resetClient // this is needed for the application to die
}
