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
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v24Tag

// downloads from zi internet!
object FolderFixer extends App with Debug {
	val folder: String = """D:\Incoming\Bittorrent\Completed\Music\Entombed.Discography.1990-2007.FLAC.CUE.Lossless\1991 - Clandestine"""
	DownloadCover.main(List(folder).toArray)	
	FixLabels.main(List(folder).toArray)
}