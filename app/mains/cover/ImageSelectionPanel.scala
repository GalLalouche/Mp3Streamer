package mains.cover

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

/** Displays several images to the user, and returns the selected image */
private class ImageSelectionPanel private(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext) {
  private val rows = 2
  private val cols = 3
  def choose(): Future[ImageChoice] = {
    val p = Promise[ImageChoice]
    val frame = new Frame {
      reactions += {case e: WindowClosing => p success Cancelled}
    }
    val panel = new AsyncFolderImagePanel(cols = cols, rows = rows, imagesSupplier = imagesSupplier)
    panel.reactions += {
      case e: ComponentAdded => frame.pack()
      case e: ImageChoice =>
        p success e
        panel.close()
    }
    frame.contents = panel
    panel.start()
    frame.open()
    val $ = p.future
    $.onComplete { e =>
      panel.close()
    }
    $
  }
}

private object ImageSelectionPanel {
  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()
}
