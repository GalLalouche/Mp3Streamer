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
	val songs = {
		val tree = new MusicFinder with MusicLocations
		tree.getSongs.map(new File(_))
	}
	val random = new Random

	val n = 50
	(1 to n)
		.map(index => (index, songs(random nextInt (songs length))))
		.foreach { case (index, file) =>
				val newFile = new File(outputDir.dir, file.name)
				FileUtils.copyFile(file, newFile)
				newFile.renameTo(new File(outputDir.dir, "%02d.%s".format(index,file.extension)))
		}
	println("Done!")
}