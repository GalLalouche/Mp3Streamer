package mains.cover

import java.io.File

import common.io.IOFile

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

/** Displays several images to the user, and returns the selected image */
private class ImageSelectionPanel private(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext) {
  private val rows = 2
  private val cols = 3
  def choose(): Future[ImageChoice] = {
    val promise = Promise[ImageChoice]
    val frame = new Frame {
      reactions += {case e: WindowClosing => promise success Cancelled}
    }
    val panel = new AsyncFolderImagePanel(cols = cols, rows = rows, imagesSupplier = imagesSupplier)
    panel.reactions += {
      case e: ComponentAdded => frame.pack()
      case e: ImageChoice =>
        promise success e
        panel.close()
    }
    frame.contents = panel
    panel.start()
    frame.open()
    val $ = promise.future
    $.onComplete { e =>
      panel.close()
      frame.dispose()
    }
    $
  }
}

private object ImageSelectionPanel {
  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()

  def main(args: Array[String]): Unit = {
    import common.RichFuture._
    implicit val ec = new ExecutionContext {
      override def reportFailure(cause: Throwable): Unit = ???
      override def execute(runnable: Runnable): Unit = runnable.run()
    }
    apply(new ImagesSupplier {
      override def next(): Future[FolderImage] = {
        val p = new FolderImage(
        new IOFile(new File("""D:\Incoming\Bittorrent\Completed\Music\Bob Dylan\1 - Studio Albums\1963 - The Freewheelin Bob Dylan\folder.jpg""")))
        Future successful p
      }
    }).get
    println("Done")
    readLine()
  }
}
