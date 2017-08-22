package mains.cover

import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}

import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future}

private trait ImagesSupplier {
  def next(): Future[FolderImage]
}
private object ImagesSupplier {
  type FolderImageDownloader = ImageSource => Future[FolderImage]
  private class SimpleImagesSupplier(urls: Iterator[ImageSource],
      downloader: FolderImageDownloader) extends ImagesSupplier {
    def next(): Future[FolderImage] = urls.next() |> downloader
  }
  def apply(urls: Iterator[ImageSource], imageDownloader: FolderImageDownloader): ImagesSupplier =
    new SimpleImagesSupplier(urls, imageDownloader)

  private class ImagesSupplierWithCache(urls: Iterator[ImageSource], downloader: FolderImageDownloader,
      cacheSize: Int, timeoutInMillis: Int)
      (implicit ec: ExecutionContext) extends ImagesSupplier {
    private val cache = new LinkedBlockingQueue[Future[FolderImage]](cacheSize)
    override def next(): Future[FolderImage] = {
      val $ = cache.poll(timeoutInMillis, TimeUnit.MILLISECONDS)
      if ($ == null)
        throw new NoSuchElementException
      fillCache()
      $
    }
    def fillCache() {
      ec.execute(new Runnable {
        override def run() {
          while (urls.hasNext && cache.size < cacheSize)
            cache.put(downloader(urls.next()))
        }
      })
    }
  }
  def withCache(urls: Iterator[ImageSource], downloader: FolderImageDownloader, cacheSize: Int,
      timeoutInMillis: Int = 5000)(implicit ec: ExecutionContext): ImagesSupplier = {
    val $ = new ImagesSupplierWithCache(urls, downloader, cacheSize, timeoutInMillis)
    $.fillCache()
    $
  }
}
