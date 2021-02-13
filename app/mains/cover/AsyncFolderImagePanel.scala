package mains.cover

import java.awt.{Color, Font}

import javax.swing.{BorderFactory, JLabel, SpringLayout, SwingConstants}
import mains.SwingUtils._

import scala.concurrent.ExecutionContext
import scala.swing.{Button, Component, Dimension, GridPanel, TextArea}

import scalaz.std.string.stringInstance
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT.richOptionT
import common.rich.func.ToMoreMonoidOps._

import common.concurrency.FutureIterant
import common.rich.RichFuture.richFuture
import common.rich.RichT._

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel(
    images: FutureIterant[FolderImage], rows: Int, cols: Int)(implicit ec: ExecutionContext)
    extends GridPanel(rows0 = rows, cols0 = cols) {
  import AsyncFolderImagePanel._

  private var current = images

  private def createImagePanel(fi: FolderImage): Component = Component.wrap(createImageLabel(fi))
      .onMouseClick(() => AsyncFolderImagePanel.this.publish(Selected(fi)))

  def refresh(): Unit = {
    contents.clear()
    // Pre-populate the grid to avoid images moving around.
    val range = 0 until rows * cols
    range.map("Placeholder for image #".+).map(new TextArea(_)).foreach(contents.+=)
    contents += Button("Fuck it, I'll do it myself!")(AsyncFolderImagePanel.this.publish(OpenBrowser))
    contents += Button("Show me more...")(refresh())
    for {
      currentIndex <- range
    } {
      val (image, next) = current.step.get.get
      current = next
      contents.synchronized {
        contents.update(currentIndex, createImagePanel(image))
        revalidate()
        contents.foreach(_.revalidate())
      }
    }
  }
}

private object AsyncFolderImagePanel {
  private val Height = 500
  private val Width = 500
  private class TextLabelProps(verticalAlignment: Int, horizontalAlignment: Int, color: Color) {
    def label(text: String): JLabel = new JLabel(text)
        .<|(_.setFont(new Font("Consolas", Font.PLAIN, 20)))
        .<|(_.setVerticalAlignment(verticalAlignment))
        .<|(_.setHorizontalAlignment(horizontalAlignment))
        .<|(_.setForeground(color))
        .<|(_.setPreferredSize(new Dimension(Width, Height)))
  }
  private val TextProps = Vector(
    // Multiple colors and locations to ensure visibility regardless of image content.
    new TextLabelProps(SwingConstants.TOP, SwingConstants.LEFT, Color.BLACK),
    new TextLabelProps(SwingConstants.TOP, SwingConstants.RIGHT, Color.GREEN),
    new TextLabelProps(SwingConstants.BOTTOM, SwingConstants.RIGHT, Color.WHITE),
    new TextLabelProps(SwingConstants.BOTTOM, SwingConstants.LEFT, Color.BLUE),
  )

  private def createImageLabel(fi: FolderImage): JLabel = {
    val fileSize = s"${fi.file.size / 1024}KB"
    val text = s"${fi.width}x${fi.height} $fileSize${" LOCAL".monoidFilter(fi.isLocal)}"
    new JLabel(fi.toIcon(Width, Height))
        .<|(_.setLayout(new SpringLayout()))
        .<|(_.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)))
        .<|(TextProps.map(_ label text) foreach _.add)
  }
}

