package mains.cover

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.swing.event.{ComponentAdded, WindowClosing}
import scala.swing.Frame

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
    val promise = Promise[ImageChoice]
    val frame =
      new Frame <| (_.reactions += { case _: WindowClosing => promise.success(Cancelled) })
    val panel = factory(images, cols = 3, rows = 2) <| (_.reactions += {
      case _: ComponentAdded =>
        frame.pack()
        frame.repaint()
      case e: ImageChoice => promise.success(e)
    })
    frame.contents = panel
    panel.refresh()
    frame.open()
    promise.future |< frame.dispose()
  }
}
