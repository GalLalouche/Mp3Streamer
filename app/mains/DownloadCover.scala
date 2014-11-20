package mains

import java.net.URL
import java.util.Random
import common.Debug
import common.io.RichStream.richStream
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.{ Image, Song }
import play.api.libs.json.{ Json, JsArray }
import play.api.libs.json.JsObject

// downloads from zi internet!
object DownloadCover extends App with Debug {
	case class CoverException(str: String) extends Exception
	val folder = args(0)//"""D:\Incoming\Bittorrent\Completed\Music\Dissection - Discography [1990-2006]\2006 - Reinkaos 320kbps"""
	def getRandomIp(): String = {
		val r = new Random
		"%s.%s.%s.%s".format(r.nextInt(256), r.nextInt(256), r.nextInt(256), r.nextInt(256))
	}

	val album = {
		val song = Song(Directory(folder).files(0))
		s"${song.artist} ${song.album}"
	}
	try {
		
	//	val externalIp: String = new URL("http://api.externalip.net/ip").openStream.readAll

	println("Searching for a cover picture for album " + album)
	val url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&rsz=8&imgsz=large".format(s"lastfm $album").replaceAll(" ", "%20")
	val jsonLine = new URL(url)
		.openConnection
		.getInputStream
		.readAll
	val json = Json.parse(jsonLine)
	val matchingSizes = (json \ "responseData" \ "results").as[JsArray].value.filter(x => {
		(x \ "width").as[String].toInt == 500 && (x \ "height").as[String].toInt == 500
	})
	val firstResult = matchingSizes.find(x => (x \ "url").as[String].toLowerCase.contains("png")).getOrElse(matchingSizes(0))
	val imageUrl = (firstResult \ "url").as[String];
	println("Downloading from url " + imageUrl)
	val f = Image(imageUrl).saveAsJpeg(Directory(folder) \ "folder.jpg")
	} catch {
		case e: Exception => throw CoverException(album)
	} 
}