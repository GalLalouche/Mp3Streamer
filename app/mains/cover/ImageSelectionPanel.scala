package mains.cover

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

import common.rich.RichFuture._
import common.rich.RichT._

/** Displays several images to the user, and returns the selected image. */
private class ImageSelectionPanel private(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext) {
  def choose(): Future[ImageChoice] = {
    val promise = Promise[ImageChoice]
    val frame = new Frame <| (_.reactions += {case _: WindowClosing => promise success Cancelled})
    val panel =
      new AsyncFolderImagePanel(cols = 3, rows = 2, imagesSupplier = imagesSupplier) <| (_.reactions += {
        case _: ComponentAdded =>
          frame.pack()
          frame.repaint()
        case e: ImageChoice => promise success e
      })
    frame.contents = panel
    panel.refresh()
    frame.open()
    promise.future |< frame.dispose()
  }
}

private object ImageSelectionPanel {
  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()
}
