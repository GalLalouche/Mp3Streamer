package mains

import java.io.File

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import common.Debug
import common.path.Directory
import common.path.RichFile.richFile
import models.Song

//TODO fix labels after (
//TODO fix roman numerals

// downloads from zi internet!
object FixLabels extends App with Debug {
	val folder = args(0)
	val lowerCaseWordsList = List("a", "am", "an", "are", "as", "at", "by", "from", "had", "has", "have", "her", "not", "but",
		"his", "in", "is", "it", "its", "me", "mine", "my", "of", "on", "our", "the", "their", "this","into", "up", "for",
		"these", "those", "them", "to", "was", "were", "will", "your", "with", "without")
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