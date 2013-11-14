package mains

import java.net.URL
import java.util.Random
import common.Debug
import common.io.RichStream.richStream
import common.path.Directory
import common.path.RichFile.richFile
import models.{Image, Song}
import play.api.libs.json.{Json, JsArray}

// downloads from zi internet!
object DownloadCover extends App with Debug {
	val folder = args(0)

	def getRandomIp(): String = {
		val r = new Random
		"%s.%s.%s.%s".format(r.nextInt(256), r.nextInt(256), r.nextInt(256), r.nextInt(256))
	}

	val album = {
		val song = Song(Directory(folder).files(0))
		song.year + " " + song.album
	}

//	val externalIp: String = new URL("http://api.externalip.net/ip").openStream.readAll

	println("Searching for a cover picture for album " + album)
	val jsonLine = new URL(
		"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&userip=%s&tbs=isz:ex,iszw:500,iszh:500".format(("lastfm " + album).replaceAll(" ", "%20"), getRandomIp()))
		.openConnection
		.getInputStream
		.readAll
	val json = Json.parse(jsonLine)
	val firstResult = (json \ "responseData" \ "results").as[JsArray].value(0)
	val imageUrl = (firstResult \ "url").as[String];
	println("Downloading from url " + imageUrl)
	val f = Image(imageUrl).saveAsJpeg(Directory(folder) \ "folder.jpg")
	Thread.sleep(100)
}