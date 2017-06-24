package mains.cover

import java.io.File

import backend.configs.StandaloneConfig
import common.io.IOFile

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}
import common.rich.RichT._

/** Displays several images to the user, and returns the selected image */
private class ImageSelectionPanel private(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext) {
  def choose(): Future[ImageChoice] = {
    val promise = Promise[ImageChoice]
    val frame = new Frame {
      reactions += { case _: WindowClosing => promise success Cancelled }
    }
    val panel = new AsyncFolderImagePanel(cols = 3, rows = 2, imagesSupplier = imagesSupplier) {
      reactions += {
        case _: ComponentAdded =>
          frame.pack()
          frame.repaint()
        case e: ImageChoice => promise success e
      }
    }
    frame.contents = panel
    panel.refresh()
    frame.open()
    val $ = promise.future
    $ onComplete { frame.dispose().const }
    $
  }
}

private object ImageSelectionPanel {
  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()

  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    implicit val c = StandaloneConfig

    val x = apply(new ImagesSupplier {
      override def next(): Future[FolderImage] = {
        Future {
          FolderImage(
            new IOFile(
              new File("""D:\Incoming\Bittorrent\Completed\Music\Bob Dylan\1 - Studio Albums\1963 - The Freewheelin Bob Dylan\folder.jpg""")))
        }
      }
    }).get
    println(x)
  }
}
