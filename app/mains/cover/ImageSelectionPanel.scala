package mains.cover

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

import common.concurrency.FutureIterant
import common.rich.RichFuture._
import common.rich.RichT._

/** Displays several images to the user, and returns the selected image. */
private class ImageSelectionPanel private(images: FutureIterant[FolderImage])(implicit ec: ExecutionContext) {
  def choose(): Future[ImageChoice] = {
    val promise = Promise[ImageChoice]
    val frame = new Frame <| (_.reactions += {case _: WindowClosing => promise success Cancelled})
    val panel =
      new AsyncFolderImagePanel(images, cols = 3, rows = 2) <| (_.reactions += {
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
  def select(images: FutureIterant[FolderImage])(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(images).choose()
}
