package mains.cover

import scala.concurrent.ExecutionContext
import scala.swing.event.MouseClicked
import scala.swing.{Button, GridPanel, Label, TextArea}

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)
                                   (implicit ec: ExecutionContext) extends GridPanel(rows0 = rows, cols0 = cols) {
  private def createImagePanel(image: FolderImage) = new Label {
    icon = image.imageIcon
    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked => AsyncFolderImagePanel.this.publish(Selected(image))
    }
  }

  // TODO consider creating a new panel instead
  def refresh() {
    contents.clear()
    // Prepopulate the grid to avoid images moving around.
    (0 until rows * cols).foreach(i => contents += new TextArea("Placeholder for image #" + i))
    contents += Button.apply("Fuck it, I'll do it myself!") {
      AsyncFolderImagePanel.this.publish(OpenBrowser)
    }
    contents += Button.apply("Show me more...") {
      refresh()
    }
    0 until (rows * cols) foreach { currentIndex =>
      imagesSupplier.next().map(createImagePanel).foreach { e =>
        contents.synchronized {
          contents.update(currentIndex, e)
          // forces a redrawing of the panel
          visible = false
          Thread sleep 10
          visible = true
        }
      }
    }
  }
}
