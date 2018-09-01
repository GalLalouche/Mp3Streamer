package mains.cover

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
    frame.contents = panel
    panel.refresh()
    frame.open()
    val $ = promise.future
    $ onComplete {frame.dispose().const}
    $
  }
}

private object ImageSelectionPanel {
  import backend.configs.StandaloneModule
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  import scala.util.Random

  def apply(imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext): Future[ImageChoice] =
    new ImageSelectionPanel(imagesSupplier).choose()

  def main(args: Array[String]): Unit = {
    import common.rich.RichFuture._
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val dir = IODirectory("""/usr/local/google/home/lalouche/Pictures""")
    val is = new ImagesSupplier {
      private val iterator =
        dir.deepFiles.iterator.filter(_.extension == "png").map(LocalSource).map(l =>
          ImageDownloader.folderImage(l.file, Random.nextBoolean(), l.width, l.height, l.image))
      override def next(): Future[FolderImage] = Future successful iterator.next()
    }

    println(apply(is).get)
  }
}
