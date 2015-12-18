package mains.cover

import java.util.concurrent.BlockingQueue
import scala.collection.LinearSeq
import java.nio.charset.MalformedInputException
import common.rich.path.Directory
import common.rich.collections.RichIterator._
import java.nio.file.Files

private class ImageQueuer(
		urls: Iterator[String], queue: BlockingQueue[FolderImage], imageDownloader: ImageDownloader) {
	private val images = urls.mapDefined(imageDownloader.download)
	def apply() {
		queue put images.next
	}
}