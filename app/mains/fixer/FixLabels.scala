package mains.fixer

import java.io.File

import common.Debug
import common.rich.RichT.richT
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import common.rich.path.RichPath.poorPath
import common.rich.primitives.RichString.richString
import models.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.ID3v24Tag

import scala.util.Try

/** Fixes ID3 tags on mp3 (and flac) files to proper casing, etc. */
object FixLabels extends App with Debug {
	private def properTrackString(track: Int): String = if (track < 10) "0" + track else track toString
	private def fixFile(f: File, fixDiscNumber: Boolean) {
		val audioFile = AudioFileIO.read(f)
		val originalTag = audioFile.getTag
		val newTag = if (f.extension.toLowerCase == "flac") new FlacTag else new ID3v24Tag

		for (f <- List(FieldKey.ARTIST, FieldKey.TITLE, FieldKey.TRACK, FieldKey.ALBUM, FieldKey.YEAR))
			newTag.setField(f, StringFixer(originalTag.getFirst(f)))
		newTag.setField(FieldKey.TRACK, properTrackString(newTag.getFirst(FieldKey.TRACK).toInt))

		if (fixDiscNumber) // change 1/2, 2/2, etc. to 1, 2, etc.
			try {
				newTag.setField(FieldKey.DISC_NO, """(\d+).*"""
					.r
					.findAllIn(originalTag.getFirst(FieldKey.DISC_NO))
					.matchData
					.next
					.group(1)
					.toInt // throws an exception if not an int string
					.toString)
			} catch {
				case e: Exception => () // do nothing... why?
			}

		AudioFileIO delete audioFile
		audioFile setTag newTag
		audioFile.commit()
	}

	private def rename(f: File) {
		Song(f).applyAndReturn(song =>
			f renameTo new File(f.parent, "%s - %s.%s".format(properTrackString(song.track), song.title, f.extension)))
	}

	private def retrieveYear(song: Song): Int =
		Try(song.year)
			.orElse(Try(song.file.parent.name.captureWith("\\D*([12]\\d{3})\\D*".r).toInt)) // Y3/0K Bug
			.recoverWith { case e: Exception => throw new Exception("Could not retrieve the year from the songs", e) }
			.get

	private val reservedDirCharacters = "<>:\"/\\|?*".toSet

	private def toLegalDirName(s: String): String = s filterNot reservedDirCharacters

	// returns the path of the output folder
	def fix(dir: Directory): Directory = {
		require(dir.files.count(_.extension == "flac") != 1 || dir.files.count(_.extension == "cue") != 1,
			"Folder contains an unsplit flac file; please split the file and try again.")

		dir.files.foreach(_.setWritable(true)) // stupid bittorrent
		dir.files.filter(_.extension == "m3u").foreach(_.delete)

		val musicFiles = dir.files.filter(f => Set("mp3", "flac") contains f.extension)
		require(musicFiles.nonEmpty, s"Could not find any songs in $dir - maybe they're in subfolders...")

		val hasNonTrivialDiscNumber = musicFiles // as opposed to 1/1 - Fuck those guys.
			.map(AudioFileIO.read)
			.map(_.getTag.getFirst(FieldKey.DISC_NO))
			.toSet
			.size > 1

		val (year, album) = musicFiles.head.mapTo(Song.apply)
			.mapTo(firstSong => retrieveYear(firstSong) -> StringFixer(firstSong.albumName))

		musicFiles foreach (fixFile(_, hasNonTrivialDiscNumber))
		musicFiles foreach rename

		try {
			val renamedFolder = new File(dir.parent, s"$year ${album |> toLegalDirName}")
			val result = dir.dir renameTo renamedFolder
      assert(result, s"Failed to rename directory to $renamedFolder")
      Directory(renamedFolder)
		} catch {
			case e: Exception => throw new Exception("could not rename the folder", e)
		}
	}
}
