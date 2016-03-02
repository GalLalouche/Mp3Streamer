package mains.cover

import java.util.concurrent.BlockingQueue

import common.rich.collections.RichIterator._

/** saves urls of images to a blocking queue */
private class ImageQueuer(
		urls: Iterator[String], queue: BlockingQueue[FolderImage], imageDownloader: ImageDownloader) {
	private val images = urls.mapDefined(imageDownloader.download)
	/** pipes one image from the urls to the blocking queue */
	def apply() {
		queue put images.next
	}
}
