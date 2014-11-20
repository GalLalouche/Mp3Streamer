package mains

import java.io.File
import scala.util.Random
import scala.util.control.Breaks.{ break, breakable }
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{ CannotWriteException, UnableToRenameFileException }
import org.jaudiotagger.tag.images.StandardArtwork
import common.rich.path.Directory
import common.rich.path.RichFile.richFile
import controllers.MusicLocations
import models.{ MusicFinder, Poster, Song }
import scala.collection.mutable.HashSet

/**
  * Selects n random songs and puts them in a folder on D
  */
object RandomFolderCreator extends App {
	val outputDir = {
		val outputFolder = new File("D:/RandomSongsOutput")
		if (outputFolder.exists == false)
			outputFolder.mkdirs
		val d = Directory(outputFolder)
		d.clear
		d
	}
	val songs = (new MusicFinder with MusicLocations).getSongFilePaths.map(new File(_))

	def createPlaylistFile(): File = {
		val files = outputDir.files
		val playlistFile = outputDir.addFile("random.m3u")
		files.map(_.name).foreach(playlistFile.appendLine(_))
		playlistFile
	}

	val random = new Random
	val n = 200
	val selectedSongs = HashSet[File]()
	def addSong {
		val index = selectedSongs.size
		val file = songs(random nextInt (songs length))
		if (selectedSongs.contains(file))
			return // cause scala doesn't have continue :\
		selectedSongs += file
		try {
			val newFile = new File(outputDir.dir, file.name)
			FileUtils.copyFile(file, newFile)
			val x = (AudioFileIO.read(newFile))
			x.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(Song(file))))
			breakable {
				try {
					x.commit
				} catch {
					case e: CannotWriteException => e.printStackTrace()
					case e: UnableToRenameFileException => e.printStackTrace()
				}
			}

			Thread.sleep(1000) // why is this here?
			newFile.renameTo(new File(outputDir.dir, "%02d.%s".format(index, file.extension)))
			println(s"${100 * index / n}%% done".format())
		} catch {
			case e: Exception => println("Failed @ " + file); e.printStackTrace; throw e
		}
	}
	while (selectedSongs.size < n) {
		addSong
	}
	createPlaylistFile
	println("Done!")
}