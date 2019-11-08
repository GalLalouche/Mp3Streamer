package mains.cover

import java.awt.{Color, Font}
import javax.swing.{JLabel, SpringLayout, SwingConstants}

import common.rich.RichT._
import mains.SwingUtils

import scala.concurrent.ExecutionContext
import scala.swing._

private object AsyncFolderImagePanel {
  private val Height = 500
  private val Width = 500
  private case class TextLabelProps(verticalAlignment: Int, horizontalAlignment: Int, color: Color) {
    def label(text: String): JLabel = {
      val $ = new JLabel(text)
      $.setFont(new Font("Consolas", Font.PLAIN, 20))
      $.setVerticalAlignment(verticalAlignment)
      $.setHorizontalAlignment(horizontalAlignment)
      $.setForeground(color)
      $.setPreferredSize(new Dimension(Width, Height))
      $
    }
  }
  private val TextProps = Seq(
    // multiple colors and locations to ensure visibility
    TextLabelProps(SwingConstants.TOP, SwingConstants.LEFT, Color.BLACK),
    TextLabelProps(SwingConstants.TOP, SwingConstants.RIGHT, Color.GREEN),
    TextLabelProps(SwingConstants.BOTTOM, SwingConstants.RIGHT, Color.WHITE),
    TextLabelProps(SwingConstants.BOTTOM, SwingConstants.LEFT, Color.BLUE),
  )

  private def fileSize(numOfBytes: Long): String = s"${numOfBytes / 1024}KB"
}

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext)
    extends GridPanel(rows0 = rows, cols0 = cols) with SwingUtils {
  import AsyncFolderImagePanel._

  private def createImagePanel(folderImage: FolderImage): Component = {
    val imageIcon = folderImage.toIcon(Width, Height)
    val text = s"${folderImage.width}x${folderImage.height} ${fileSize(folderImage.file.size)}" +
        " LOCAL".onlyIf(folderImage.isLocal)
    val imageLabel = new JLabel(imageIcon)
    imageLabel.setLayout(new SpringLayout())
    TextProps.map(_ label text) foreach imageLabel.add
    Component.wrap(imageLabel).onMouseClick(() => AsyncFolderImagePanel.this.publish(Selected(folderImage)))
  }

  def refresh(): Unit = {
    contents.clear()
    // Pre-populate the grid to avoid images moving around.
    val range = 0 until rows * cols
    range.map("Placeholder for image #".+).map(new TextArea(_)).foreach(contents.+=)
    contents += Button.apply("Fuck it, I'll do it myself!")(AsyncFolderImagePanel.this.publish(OpenBrowser))
    contents += Button("Show me more...")(refresh())
    for (currentIndex <- range;
         image <- imagesSupplier.next().map(createImagePanel)) {
      contents.synchronized {
        contents.update(currentIndex, image)
        revalidate()
        contents.foreach(_.revalidate())
      }
    }
  }
}
