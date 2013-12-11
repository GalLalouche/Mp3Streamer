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

//TODO fix roman numerals

// downloads from zi internet!
object FixLabels extends App with Debug {
	private lazy val lowerCaseWordsList = List("a", "am", "an", "and", "are", "as", "at", "but", "by", "can", "can't", "cannot",
		"do", "don't", "for", "from", "had", "has", "have", "her", "his", "in", "into", "is", "it", "it's", "its",
		"me", "mine", "my", "not", "of", "on", "or", "our", "that", "the", "their", "them", "these", "this", "those", "to",
		"up", "was", "were", "will", "with", "without", "won't", "would", "wouldn't", "your")
	private lazy val lowerCaseWords = lowerCaseWordsList.toSet
	if (lowerCaseWords.toList.sorted != lowerCaseWordsList.sorted)
		println(lowerCaseWords.toList.sorted.map(""""%s"""".format(_)))

	private def properTrackString(track: Int): String = if (track < 10) "0" + track else track toString

	private def fixString(s: String): String = {
		def upperCaseWord(w: String): String = w(0).toUpper + w.drop(1)
		def fixWord(w: String): String = w match {
			case _ if (w.length == 1) => w
			case _ if (lowerCaseWords(w)) => w toLowerCase
			case _ if (w.startsWith("(")) => "(" + fixWord(w drop 1)
			case _ => upperCaseWord(w)
		}
		val split = s.split("\\s+").toList.map(_.toLowerCase)
		(upperCaseWord(split(0)) :: (split.drop(1).map(fixWord).toList)).mkString(" ")
	}

	private def fixFile(f: File) {
		val audioFile = AudioFileIO.read(f)
		val originalTag = audioFile.getTag
		originalTag.deleteArtworkField
		val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
		List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR)
			.foreach(f => newTag.setField(f, fixString(originalTag.getFirst(f))))
		newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))
		AudioFileIO.delete(audioFile)
		audioFile.setTag(newTag)
		audioFile.commit
	}

	private def rename(f: File) {
		val song = Song(f)
		f.renameTo("%s - %s.%s".format(properTrackString(song.track), song.title, f.extension))
	}
	
	def fix(folder: String): String = {
		val d = Directory(folder).cloneDir
		val files = d
			.files
			.filter(f => Set("mp3", "flac").contains(f.extension))
		files.foreach(fixFile)
		files.foreach(rename)
		val firstSong = Song(d.files(0))
		d.renameTo("%s %s".format(firstSong.year, firstSong.album))
		d.path
	}
	
	fix("""D:\Incoming\Bittorrent\Completed\Music\Whispered -2010- Thousand Swords""")
}