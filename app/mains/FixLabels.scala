package mains

import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import common.Debug
import common.path.Directory
import common.path.Path.poorPath
import common.path.RichFile.richFile
import models.Song
import org.jaudiotagger.tag.KeyNotFoundException

object FixLabels extends App with Debug {
	private lazy val lowerCaseWordsList = List("a", "ain't", "am", "an", "and", "are", "as", "at", "be", "but", "by", "can", "can't", "cannot",
		"do", "don't", "for", "from", "had", "has", "have", "her", "his", "in", "into", "is", "it", "it's", "its",
		"me", "mine", "my", "not", "of", "on", "or", "our", "so", "should", "that", "the", "their", "them", "these",
		"this", "those", "did", "to", "too", "up", "was", "were", "will", "with", "without", "won't", "would", "wouldn't",
		"your", "upon", "shall", "may", "there", "ov")
	private lazy val lowerCaseWords = lowerCaseWordsList.toSet
	if (lowerCaseWords.toList.sorted != lowerCaseWordsList.sorted)
		println(lowerCaseWords.toList.sorted.map(""""%s"""".format(_)))

	private def properTrackString(track: Int): String = if (track < 10) "0" + track else track toString

	private def fixString(s: String): String = {
		def upperCaseWord(w: String): String = w(0).toUpper + w.drop(1)
		def fixWord(w: String): String = w match {
			case "a" => "a" // don't know why this needs to be handled explicitly
			case s if (s matches "[IVXivx]+") => s toUpperCase // roman numbers
			case _ if (w matches """^[(\[].+""") => w(0) + fixWord(w drop 1) // words starting with (
			case _ => if (lowerCaseWords(w.toLowerCase)) w.toLowerCase else upperCaseWord(w) // everything else
		}
		val split = s split "\\s+" map (_.toLowerCase)
		(upperCaseWord(split(0)) :: (split drop 1 map fixWord toList)) mkString " "
	}

	private def fixFile(f: File, fixDiscNumber: Boolean) {
		val audioFile = AudioFileIO.read(f)
		val originalTag = audioFile.getTag
		originalTag.deleteArtworkField
		val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
		List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR)
			.foreach(f => newTag.setField(f, fixString(originalTag.getFirst(f))))
		newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))
		if (fixDiscNumber)
			try {
				newTag.setField(FieldKey.DISC_NO, """(\d+).*"""
					.r
					.findAllIn(originalTag.getFirst(FieldKey.DISC_NO))
					.matchData
					.toList(0)
					.group(1)
					.toInt // throws an exception if not an int string
					.toString)
			} catch {
				case e: Exception => () // do nothing	
			}

		AudioFileIO delete audioFile
		audioFile setTag newTag
		audioFile commit
	}

	private def rename(f: File) {
		val song = Song(f)
		f renameTo new File(f.parent, "%s - %s.%s".format(properTrackString(song.track), song.title, f.extension))
	}

	private def retrieveYear(firstSong: Song): Int = {
		try firstSong.year
		catch {
			case _: Exception => firstSong.file.parent.name.split("[-\\s]+")(0).toInt
		}
	}

	// returns the path of the output folder
	def fix(folder: String): String = {
		val dir = Directory(folder).cloneDir
		if (dir.files.filter(_.extension == "flac").size == 1 && dir.files.filter(_.extension == "cue").size == 1)
			throw new IllegalArgumentException("Folder contains an unsplit flac file; please split the file and try again.")
		dir
			.files
			.filter(_.extension == "m3u")
			.foreach(_.delete)
		val files = dir
			.files
			.filter(f => Set("mp3", "flac").contains(f.extension))
		require(files.nonEmpty, "Could not find any songs - could it be hidden in a subfolder?")
		val hasRealDiscNumber = files
			.map(AudioFileIO
				.read(_)
				.getTag
				.getFirst(FieldKey.DISC_NO))
			.toSet
			.size > 1
		val firstSong = Song(files(0))
		files foreach (fixFile(_, hasRealDiscNumber))
		files foreach rename
		val year = try { retrieveYear(firstSong) } catch { case e: Exception => throw new Exception("Could not retrieve the year", e) }
		try {
			val renamedFolder = new File(dir.parent, "%s %s".format(year, fixString(firstSong.album)))
			dir.dir renameTo renamedFolder
			renamedFolder getAbsolutePath
		} catch { case e: Exception => throw new Exception("could not rename the folder", e) }
	}

	println(fixString("Living on A Nightmare"))
}