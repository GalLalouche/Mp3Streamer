package mains.cover

import java.awt.Dimension
import javax.swing.ImageIcon

import backend.configs.CleanConfiguration
import common.io.IODirectory

import scala.concurrent.{ExecutionContext, Future}
import scala.swing.event.MouseClicked
import scala.swing.{Button, GridPanel, Label, TextArea}

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)
    (implicit ec: ExecutionContext) extends GridPanel(rows0 = rows, cols0 = cols) {
  private def createImagePanel(image: FolderImage): Label = new Label {
    icon = new ImageIcon(image.file.path)
    listenTo(mouse.clicks)
    reactions += {
      case _: MouseClicked => AsyncFolderImagePanel.this.publish(Selected(image))
    }
    preferredSize = new Dimension(500, 500)
    tooltip = s"${icon.getIconHeight}✕${icon.getIconWidth}"
  }

  // TODO consider creating a new panel instead
  def refresh() {
    contents.clear()
    // Pre-populate the grid to avoid images moving around.
    (0 until rows * cols).foreach(i => contents += new TextArea(s"Placeholder for image #$i"))
    contents += Button.apply("Fuck it, I'll do it myself!") {
      AsyncFolderImagePanel.this.publish(OpenBrowser)
    }
    contents += Button.apply("Show me more...") {
      refresh()
    }
    for (currentIndex <- 0 until (rows * cols);
         image <- imagesSupplier.next().map(createImagePanel)) {
      contents.synchronized {
        val height = image.size.height
        val width = image.size.width
        image.size
        contents.update(currentIndex, image)
        // forces a redrawing of the panel
        visible = false
        Thread sleep 10
        visible = true
      }
    }
  }
}

object AsyncFolderImagePanel {
  def main(args: Array[String]): Unit = {
    implicit val c = CleanConfiguration
    val dir = IODirectory("""D:\Media\Music\Rock\Classic Rock""")
    val $ = new AsyncFolderImagePanel(2, 4, new ImagesSupplier {
      val iterator = dir.deepFiles.iterator.filter(_.extension == "jpg").map(FolderImage.apply)
      override def next(): Future[FolderImage] = Future {iterator.next()}
    })
  }
}
