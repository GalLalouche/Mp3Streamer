package other

import models.Album
import play.api.libs.json._
import play.api.libs.ws.WS

trait MetadataRetriever {
	protected def getArtistJson(artist: String): JsArray
	
	protected def jsonToAlbum(js: JsValue): Option[Album]
	
			protected implicit def richJson(js: JsValue) = new {
		def asString: String = js.asInstanceOf[JsString].value
	}
	def getAlbums(artist: String): Iterator[Album] =
		try {
			getArtistJson(artist)
				.value
				.iterator
				.map(jsonToAlbum)
				.collect { case Some(a) => a }
		} finally {
			WS.resetClient // this is needed for the application to die
		}
}
