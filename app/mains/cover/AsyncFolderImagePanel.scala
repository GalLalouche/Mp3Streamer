package mains.cover

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, GridLayout}
import javax.swing.{JLabel, SwingConstants}

import common.rich.RichT._

import scala.concurrent.ExecutionContext
import scala.swing._

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)
    (implicit ec: ExecutionContext) extends GridPanel(rows0 = rows, cols0 = cols) {
  private def createImagePanel(folderImage: FolderImage): Component = {
    case class TextLabelProps(verticalAlignment: Int, horizontalAlignment: Int, color: Color) {
      def label(text: String): JLabel = {
        val $ = new JLabel(text)
        $.setFont($.getFont.deriveFont(10))
        $.setVerticalAlignment(verticalAlignment)
        $.setHorizontalAlignment(horizontalAlignment)
        $.setForeground(color)
        $
      }
    }
    val textProps = Seq(
      // multiple colors and locations to ensure visibility
      TextLabelProps(SwingConstants.TOP, SwingConstants.LEFT, Color.BLACK),
      TextLabelProps(SwingConstants.TOP, SwingConstants.RIGHT, Color.GREEN),
      TextLabelProps(SwingConstants.BOTTOM, SwingConstants.RIGHT, Color.WHITE),
      TextLabelProps(SwingConstants.BOTTOM, SwingConstants.LEFT, Color.BLUE)
    )
    val imageIcon = folderImage.toIcon(500, 500)
    val text = s"${folderImage.width}✕${folderImage.height} ${"(LOCAL)".onlyIf(folderImage.isLocal)}"
    val imageLabel = new JLabel(imageIcon)
    imageLabel.setLayout(new GridLayout())
    textProps.map(_.label(text)).foreach(imageLabel.add)
    imageLabel.addMouseListener(new MouseListener {
      override def mouseExited(e: MouseEvent) = ()
      override def mousePressed(e: MouseEvent) = ()
      override def mouseReleased(e: MouseEvent) = ()
      override def mouseEntered(e: MouseEvent) = ()
      override def mouseClicked(e: MouseEvent) = AsyncFolderImagePanel.this.publish(Selected(folderImage))
    })
    Component wrap imageLabel
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
