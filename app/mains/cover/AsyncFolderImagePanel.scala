package mains.cover

import scala.concurrent.ExecutionContext
import scala.swing.event.MouseClicked
import scala.swing.{Button, GridPanel, Label, TextArea}

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)
    (implicit ec: ExecutionContext) extends GridPanel(rows0 = rows, cols0 = cols) {
  private def createImagePanel(folderImage: FolderImage): Label = new Label {
    // TODO extract
    icon = folderImage.toIcon(500, 500)
    listenTo(mouse.clicks)
    reactions += {
      case _: MouseClicked => AsyncFolderImagePanel.this.publish(Selected(folderImage))
    }
    tooltip = s"${folderImage.height}✕${folderImage.width}"
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
