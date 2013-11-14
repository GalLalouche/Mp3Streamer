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
import javax.sound.sampled.AudioInputStream
import org.jaudiotagger.tag.flac.FlacTag

// downloads from zi internet!
object FixLabels extends App with Debug {
	val folder = args(0)
	val lowerCaseWordsList = List("a", "am", "an", "are", "as", "from", "had", "has", "have", "her",
		"his", "in", "is", "it", "its", "mine", "my", "of", "on", "our", "the", "their", "this",
		"to", "was", "were", "will", "your")
	val lowerCaseWords = lowerCaseWordsList.toSet
	if (lowerCaseWords.toList.sorted != lowerCaseWordsList.sorted)
		println(lowerCaseWords.toList.sorted.map(""""%s"""".format(_)))

	def properTrackString(track: Int): String = if (track < 10) "0" + track else track toString

	def fix(s: String): String = {
		def upperCaseWord(w: String): String = w(0).toUpper + w.drop(1)
		def fixWord(w: String): String = if (lowerCaseWords(w)) w toLowerCase else upperCaseWord(w)
		val split = s.split("\\s+").toList.map(_.toLowerCase)
		(upperCaseWord(split(0)) :: (split.drop(1).map(fixWord).toList)).mkString(" ")
	}

	def fix(f: File) {
		val audioFile = AudioFileIO.read(f)
		val originalTag = audioFile.getTag
		originalTag.deleteArtworkField
		val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
		List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR)
			.foreach(f => newTag.setField(f, fix(originalTag.getFirst(f))))
		newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))
		AudioFileIO.delete(audioFile)
		audioFile.setTag(newTag)
		audioFile.commit
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