package mains.cover

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import common.rich.RichT._

import scala.concurrent.Future

private trait ImagesSupplier {
  def next(): Future[FolderImage]
}
private object ImagesSupplier {
  private class SimpleImagesSupplier(urls: Iterator[String], imageDownloader: ImageDownloader) extends ImagesSupplier {
    def next(): Future[FolderImage] = urls.next() |> imageDownloader.download
  }
  def apply(urls: Iterator[String], imageDownloader: ImageDownloader): ImagesSupplier =
    new SimpleImagesSupplier(urls: Iterator[String], imageDownloader: ImageDownloader)

  private class ImagesSupplierWithCache(urls: Iterator[String], downloader: ImageDownloader, cacheSize: Int) extends ImagesSupplier {
    private val cache = new LinkedBlockingQueue[Future[FolderImage]](cacheSize)
    override def next(): Future[FolderImage] = cache.poll(5, TimeUnit.SECONDS)
    def startCaching() {
      val t = new Thread {override def run {urls.map(downloader.download).foreach(cache.put)}}
      t.setDaemon(true)
      t.start()
    }
  }
  def withCache(urls: Iterator[String], imageDownloader: ImageDownloader, cacheSize: Int): ImagesSupplier = {
    val $ = new ImagesSupplierWithCache(urls: Iterator[String], imageDownloader: ImageDownloader, cacheSize: Int)
    $.startCaching()
    $
  }
}
