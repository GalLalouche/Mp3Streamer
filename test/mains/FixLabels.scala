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
object FixLabels extends App with Debug {
	val folder = args(0)
	val lowerCaseWords = Set("of", "the", "is", "are", "am", "in", "a", "an", "as")
	def fix(s: String): String = {
		s.split(" ").drop(0).map(x => if (lowerCaseWords(x)) x.toLowerCase else x).mkString(" ")
	}

	def properTrackString(track: Int): String = if(track < 10) "0" + track else track toString
	
	def fix(f: File) {
		def clearUselessData {
			val audioFile = AudioFileIO.read(f)
			val originalTag = audioFile.getTag
			originalTag.deleteArtworkField
			val newTag = new ID3v24Tag
			List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR)
				.foreach(f => newTag.setField(f, fix(originalTag.getFirst(f))))
			newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))
			audioFile.setTag(newTag)
			audioFile.commit
		}
		clearUselessData
	}
	
	def rename(f: File) {
		val song = Song(f)
		f.renameTo("%s - %s.%s".format(properTrackString(song.track), song.title, f.extension))
	}

	val d = Directory(folder).cloneDir
	val files = d
		.files
		.filter(f => Set("mp3", "flac").contains(f.extension))
	files.foreach(fix)
	files.foreach(rename)
	val firstSong = Song(d.files(0))
	d.renameTo("%s %s".format(firstSong.year, firstSong.album))
}