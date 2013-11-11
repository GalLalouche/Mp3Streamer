package mains

import models.MusicFinder
import common.path.Directory
import java.io.File
import common.path.RichFile._
import org.joda.time.DateTime
import common.Debug
import loggers.ConsoleLogger
import loggers.ConsoleLogger
import java.net.URL
import play.api.libs.json.Json
import java.net.InetAddress
import java.io.BufferedReader
import java.io.InputStreamReader
import common.io.RichStream._
import play.api.libs.json.JsArray
import models.Image
import java.util.Random
import models.Song

// downloads from zi internet!
object DownloadCover extends App with Debug {
	val folder = """D:\Incoming\Bittorrent\Completed\Music\2011 Soul Punk"""

	def getRandomIp(): String = {
		val r = new Random
		"%s.%s.%s.%s".format(r.nextInt(256), r.nextInt(256), r.nextInt(256), r.nextInt(256))
	}

	def getAlbum: String = {
		val song = Song(Directory(folder).files(0))
		song.year + " " + song.album
	}

	val externalIp: String = new URL("http://api.externalip.net/ip").openStream.readAll

	println("Searching for a cover picture for album " + getAlbum)
	val jsonLine = new URL(
		"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&userip=%s&tbs=isz:ex,iszw:500,iszh:500".format(("lastfm " + getAlbum).replaceAll(" ", "%20"), getRandomIp()))
		.openConnection
		.getInputStream
		.readAll
	val json = Json.parse(jsonLine)
	//"{\"responseData\":{\"results\":[{\"GsearchResultClass\":\"GimageSearch\",\"width\":\"400\",\"height\":\"400\",\"imageId\":\"ANd9GcRkd1Oy8izNY-IaeEF-XZpTJf13bb26W1smfzD59f3CiHmDl-XyiokT1w\",\"tbWidth\":\"124\",\"tbHeight\":\"124\",\"unescapedUrl\":\"http://userserve-ak.last.fm/serve/_/31661169/The+Fillmore+Concerts+disc+2+front_cover_small.jpg\",\"url\":\"http://userserve-ak.last.fm/serve/_/31661169/The%2BFillmore%2BConcerts%2Bdisc%2B2%2Bfront_cover_small.jpg\",\"visibleUrl\":\"www.last.fm\",\"title\":\"<b>The Fillmore Concerts</b> (disc 2) – <b>The</b> Allman Brothers Band – Listen <b>...</b>\",\"titleNoFormatting\":\"The Fillmore Concerts (disc 2) – The Allman Brothers Band – Listen ...\",\"originalContextUrl\":\"http://www.last.fm/music/The+Allman+Brothers+Band/The+Fillmore+Concerts+(disc+2)\",\"content\":\"<b>The Fillmore Concerts</b> (disc 2) – <b>The</b> Allman Brothers Band – Listen\",\"contentNoFormatting\":\"The Fillmore Concerts (disc 2) – The Allman Brothers Band – Listen\",\"tbUrl\":\"http://t3.gstatic.com/images?q=tbn:ANd9GcRkd1Oy8izNY-IaeEF-XZpTJf13bb26W1smfzD59f3CiHmDl-XyiokT1w\"},{\"GsearchResultClass\":\"GimageSearch\",\"width\":\"252\",\"height\":\"189\",\"imageId\":\"ANd9GcQs_AbvmsxAvKsnKYFgiZuZ2jL0JAULyWs4wPzz5yL35P5axCHHSPrwnTQ\",\"tbWidth\":\"111\",\"tbHeight\":\"83\",\"unescapedUrl\":\"http://userserve-ak.last.fm/serve/252/529043.jpg\",\"url\":\"http://userserve-ak.last.fm/serve/252/529043.jpg\",\"visibleUrl\":\"www.last.fm\",\"title\":\"529043.jpg\",\"titleNoFormatting\":\"529043.jpg\",\"originalContextUrl\":\"http://www.last.fm/venue/8778982+The+Fillmore\",\"content\":\"529043.jpg\",\"contentNoFormatting\":\"529043.jpg\",\"tbUrl\":\"http://t0.gstatic.com/images?q=tbn:ANd9GcQs_AbvmsxAvKsnKYFgiZuZ2jL0JAULyWs4wPzz5yL35P5axCHHSPrwnTQ\"},{\"GsearchResultClass\":\"GimageSearch\",\"width\":\"252\",\"height\":\"187\",\"imageId\":\"ANd9GcSR_F9W4aHs7P5znA3KCeKG9rccva2XzUIzYDrZ0UaemgT4fIolwH0zKg\",\"tbWidth\":\"111\",\"tbHeight\":\"82\",\"unescapedUrl\":\"http://userserve-ak.last.fm/serve/252/5137105.jpg\",\"url\":\"http://userserve-ak.last.fm/serve/252/5137105.jpg\",\"visibleUrl\":\"www.last.fm\",\"title\":\"5137105.jpg\",\"titleNoFormatting\":\"5137105.jpg\",\"originalContextUrl\":\"http://www.last.fm/venue/8852755+The+Fillmore+Detroit\",\"content\":\"5137105.jpg\",\"contentNoFormatting\":\"5137105.jpg\",\"tbUrl\":\"http://t2.gstatic.com/images?q=tbn:ANd9GcSR_F9W4aHs7P5znA3KCeKG9rccva2XzUIzYDrZ0UaemgT4fIolwH0zKg\"},{\"GsearchResultClass\":\"GimageSearch\",\"width\":\"252\",\"height\":\"142\",\"imageId\":\"ANd9GcTodo4dCodknmjLLWZQjUUiB5Z_8e0kkGiZrL0IkqWpUt2BzyYo6SDbgeo\",\"tbWidth\":\"111\",\"tbHeight\":\"63\",\"unescapedUrl\":\"http://userserve-ak.last.fm/serve/252/38220827.jpg\",\"url\":\"http://userserve-ak.last.fm/serve/252/38220827.jpg\",\"visibleUrl\":\"www.last.fm\",\"title\":\"38220827.jpg\",\"titleNoFormatting\":\"38220827.jpg\",\"originalContextUrl\":\"http://www.last.fm/venue/9036632+The+Fillmore+Charlotte\",\"content\":\"38220827.jpg\",\"contentNoFormatting\":\"38220827.jpg\",\"tbUrl\":\"http://t1.gstatic.com/images?q=tbn:ANd9GcTodo4dCodknmjLLWZQjUUiB5Z_8e0kkGiZrL0IkqWpUt2BzyYo6SDbgeo\"}],\"cursor\":{\"resultCount\":\"5,880\",\"pages\":[{\"start\":\"0\",\"label\":1},{\"start\":\"4\",\"label\":2},{\"start\":\"8\",\"label\":3},{\"start\":\"12\",\"label\":4},{\"start\":\"16\",\"label\":5},{\"start\":\"20\",\"label\":6},{\"start\":\"24\",\"label\":7},{\"start\":\"28\",\"label\":8}],\"estimatedResultCount\":\"5880\",\"currentPageIndex\":0,\"moreResultsUrl\":\"http://www.google.com/images?oe=utf8&ie=utf8&source=uds&start=0&hl=en&q=lastfm+The+filmore+concerts\",\"searchResultTime\":\"0.37\"}},\"responseDetails\":null,\"responseStatus\":200}")
	println(json)
	val firstResult = (json \ "responseData" \ "results").as[JsArray].value(0)
	val imageUrl = (firstResult \ "url").as[String];
	println("Downloading from url " + imageUrl)
	val f = Image(imageUrl).saveAsJpeg(Directory(folder) \ "folder.jpg").openWithDefaultApplication
}