package mains

import java.io.File
import common.path.Directory
import common.path.RichFile._
import models.MusicFinder
import controllers.MusicLocations
import models.Song
import scala.util.Random
import org.apache.commons.io.IOUtils
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.images.StandardArtwork
import models.Album
import models.Poster
import org.jaudiotagger.audio.exceptions.CannotWriteException
import scala.util.control.Breaks._
import org.jaudiotagger.audio.exceptions.UnableToRenameFileException

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

	val random = new Random
	val n = 100
	(1 to n)
		.map(index => (index, songs(random nextInt (songs length))))
		.foreach { case (index, file) =>
			val newFile = new File(outputDir.dir, file.name)
			FileUtils.copyFile(file, newFile)
			val x = (AudioFileIO.read(newFile))
			x.getTag.setField(StandardArtwork.createArtworkFromFile(Poster.getCoverArt(Song(file))))
			breakable {while (true){
				try {
					x.commit
					break
				} catch {
					case _: CannotWriteException => ()
					case _: UnableToRenameFileException => ()
				}
			}}
			
			Thread.sleep(100)
			newFile.renameTo(new File(outputDir.dir, "%02d.%s".format(index, file.extension)))
			println(s"${100 * index / n}%% done".format())
		}
	println("Done!")
}