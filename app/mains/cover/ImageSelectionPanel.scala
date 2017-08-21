package mains.cover

import javax.swing.ToolTipManager

import backend.configs.StandaloneConfig
import common.io.IODirectory
import common.rich.RichT._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

/** Displays several images to the user, and returns the selected image */
private class ImageSelectionPanel private(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext) {
  def choose(): Future[ImageChoice] = {
    val promise = Promise[ImageChoice]
    val frame = new Frame {
      reactions += {case _: WindowClosing => promise success Cancelled}
    }
    val panel = new AsyncFolderImagePanel(cols = 3, rows = 2, imagesSupplier = imagesSupplier) {
      reactions += {
        case _: ComponentAdded =>
          frame.pack()
          frame.repaint()
        case e: ImageChoice => promise success e
      }
    }
    ToolTipManager.sharedInstance().setInitialDelay(0)
    frame.contents = panel
    panel.refresh()
    frame.open()
    val $ = promise.future
    $ onComplete {frame.dispose().const}
    $
  }
}

private object ImageSelectionPanel {
  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()

  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    implicit val c = StandaloneConfig
    val dir = IODirectory("""/usr/local/google/home/lalouche/Downloads""")
    val is = new ImagesSupplier {
      val iterator = dir.deepFiles.iterator.filter(_.extension == "jpg").map(FolderImage.apply)
      override def next(): Future[FolderImage] = Future {iterator.next()}
    }

    val x = apply(is).get
    println(x)
  }
}
