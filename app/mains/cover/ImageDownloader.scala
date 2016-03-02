package mains.cover

import java.io.File

import common.rich.collections.RichTraversable._
import common.rich.path.Directory
import common.rich.path.RichFile.poorFile

import scala.util.Try

/** 
 *  Downloads images and saves them to a directory. Tries several different unicode encodings.
 *  @param outputDirectory The directory to save images to
 *  @param downloader Used to download the images
 */
private class ImageDownloader(outputDirectory: Directory, downloader: Downloader) {
	def this(outputDirectory: Directory) = this(outputDirectory, new Downloader)

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
