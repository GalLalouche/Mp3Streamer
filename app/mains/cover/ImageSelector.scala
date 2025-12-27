package mains.cover

import com.google.inject.Inject
import mains.cover.ImageSelector._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

import common.concurrency.FutureIterant
import common.rich.RichFuture._
import common.rich.RichT._

/** Displays several images to the user, and returns the selected image. */
private class ImageSelector @Inject() (
    factory: AsyncFolderImagePanelFactory,
    ec: ExecutionContext,
)() {
  private implicit val iec: ExecutionContext = ec
  def select(images: FutureIterant[FolderImage]): Future[ImageChoice] = {
    val $ = Promise[ImageChoice]()
    val frame = new Frame <| (_.reactions += { case _: WindowClosing => $.success(Cancelled) })
    val panel = factory(images, cols = Cols, rows = Rows) <| (_.reactions += {
      case _: ComponentAdded =>
        frame.pack()
        frame.repaint()
      case e: ImageChoice => $.success(e)
    })
    frame.contents = panel
    panel.refresh()
    frame.open()
    $.future |< frame.dispose()
  }
}

private object ImageSelector {
  val Cols: Int = 3
  val Rows: Int = 2
  val ImagesPerPage: Int = Cols * Rows
}
