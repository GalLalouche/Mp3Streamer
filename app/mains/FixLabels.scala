package mains

import java.io.File
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag
import common.Debug
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import models.Song
import org.jaudiotagger.tag.KeyNotFoundException

object FixLabels extends App with Debug {
	private def properTrackString(track: Int): String = if (track < 10) "0" + track else track toString
	private def fixFile(f: File, fixDiscNumber: Boolean) {
		val audioFile = AudioFileIO.read(f)
		val originalTag = audioFile.getTag
		originalTag.deleteArtworkField
		val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag
		List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR)
			.foreach { f =>
				newTag.setField(f, StringFixer(originalTag.getFirst(f)))
			}
		newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))
		if (fixDiscNumber) // change 1/2, 2/2, etc. to 1, 2, etc.
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

	private def retrieveYear(song: Song): Int = {
		try song.year
		catch {
			case _: Exception => song.file.parent.name.split("[-\\s]+")(0).toInt
		}
	}

	// returns the path of the output folder
	def fix(dir: Directory) = {
		if (dir.files.count(_.extension == "flac") == 1 && dir.files.count(_.extension == "cue") == 1)
			throw new IllegalArgumentException("Folder contains an unsplit flac file; please split the file and try again.")
		dir.files.foreach(_.setWritable(true))
		dir.files.filter(_.extension == "m3u").foreach(_.delete)
		val musicFiles = dir.files.filter(f => Set("mp3", "flac") contains f.extension)
		require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")
		val firstSong = Song(musicFiles(0))
		val year = try
			retrieveYear(firstSong)
		catch {
			case e: Exception => throw new Exception("Could not retrieve the year from the songs", e)
		}
		val hasNonTrivialDiscNumber = musicFiles // as opposed to 1/1 - Fuck those guys.
			.map(AudioFileIO
				.read(_)
				.getTag
				.getFirst(FieldKey.DISC_NO))
			.toSet
			.size > 1
		musicFiles foreach (fixFile(_, hasNonTrivialDiscNumber))
		musicFiles foreach rename
		try {
			val renamedFolder = new File(dir.parent, s"$year ${StringFixer apply firstSong.album}")
			dir.dir renameTo renamedFolder
			renamedFolder getAbsolutePath
		} catch { case e: Exception => throw new Exception("could not rename the folder", e) }
	}
}