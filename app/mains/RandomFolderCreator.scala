package mains

import java.io.File

import scala.util.Random
import scala.util.control.Breaks.{ break, breakable }

import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.{ CannotWriteException, UnableToRenameFileException }
import org.jaudiotagger.tag.images.StandardArtwork

import common.path.Directory
import common.path.RichFile.richFile
import controllers.MusicLocations
import models.{ MusicFinder, Poster, Song }

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
	val songs = (new MusicFinder with MusicLocations).getSongs.map(new File(_))

	def createPlaylistFile(): File = {
		val files = outputDir.files
		val playlistFile = outputDir.addFile("random.m3u")
		files.map(_.name).foreach(playlistFile.appendLine(_))
		playlistFile
	}

	val random = new Random
	val n = 100
	(1 to n)
		.map(index => (index, songs(random nextInt (songs length))))
		.foreach {
			case (index, file) =>
				try {
					val newFile = new File(outputDir.dir, file.name)
					FileUtils.copyFile(file, newFile)
					val x = (AudioFileIO.read(newFile))
					x.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(Song(file))))
					breakable {
						while (true) {
							try {
								x.commit
								break
							} catch {
								case _: CannotWriteException => ()
								case _: UnableToRenameFileException => ()
							}
						}
					}

					Thread.sleep(100) // why is this here?
					newFile.renameTo(new File(outputDir.dir, "%02d.%s".format(index, file.extension)))
					println(s"${100 * index / n}%% done".format())
				} catch {
					case e: Exception => println("Failed @ " + file); e.printStackTrace; throw e
				}
		}
	createPlaylistFile
	println("Done!")
}