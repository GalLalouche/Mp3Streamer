package mains.cover

import java.nio.charset.MalformedInputException
import scala.collection.LinearSeq
import common.rich.path.Directory
import common.rich.path.RichFile.poorFile
import common.rich.collections.RichTraversable._
import common.rich.path.RichFile.poorFile
import scala.io.Source
import java.io.File
import scala.util.Try

/** 
 *  Downloads images and saves them to a directory. Tries several different unicode encodings.
 *  @param outputDirectory The directory to save images to
 *  @param downloader Used to download the images
 */
private class ImageDownloader(outputDirectory: Directory, downloader: Downloader) {
	def this(outputDirectory: Directory) = this(outputDirectory, new Downloader)
	/**
	  * Tries to get the bytes with a list of encodings
	  * @param encodings The list of encodings to use, one after the other
	  */
	private def getBytes(url: String, encoding: String): Option[Array[Byte]] = {
		Try(downloader.download(url, encoding)).toOption
	}

	private def toFile(bytes: Array[Byte]): File =
		outputDirectory.addFile(System.currentTimeMillis() + "img.jpg").write(bytes)

	def download(url: String): Option[FolderImage] = {
		List("ISO-8859-1", "Cp1252", "UTF-8", "UTF-16")
			.view
			.mapDefined(getBytes(url, _))
			.map(toFile)
			.map(FolderImage)
			.headOption
	}
}