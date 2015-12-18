package mains

import java.io.File
import scala.swing.event.WindowClosing
import scala.swing.event.MouseClicked
import scala.swing.GridPanel
import scala.swing.Frame
import java.util.concurrent.LinkedBlockingQueue
import scala.swing.Label

/**
  * Displays several images to the user, and returns the selected image
  */
private[mains] class ImageSelectionPanel private (images: Iterator[FolderImage]) {
	private abstract sealed class Choice
	private case object Next extends Choice
	private case class Selected(val i: FolderImage) extends Choice
	private case object Closed extends Choice
	def choose: FolderImage = {
		val waiter = new LinkedBlockingQueue[Choice]
		val frame = new Frame {
			reactions += {
				case e: WindowClosing => waiter put Closed
			}
		}
		val panel = new GridPanel(cols0 = 3, rows0 = 2)
		val thread = new Thread("ImageDownloadingThread") {
			def maxSize = panel.rows * panel.columns
			def downloadImage(totalImages: Int = 0) {
				if (isInterrupted || totalImages >= maxSize)
					return
				val img = images.next()
				val l = new Label {
					icon = img.imageIcon
					listenTo(mouse.clicks)
					reactions += {
						case e: MouseClicked => waiter put Selected(img)
					}
				}
				panel.contents += l
				panel.repaint()
				frame.repaint()
				frame.pack()
				downloadImage()
			}
			override def run() {
				downloadImage()
			}
		}
		thread setDaemon true
		thread.start()
		try {
			frame.contents = panel
			frame.open()
			waiter.take match {
				case Selected(f) => return f
				case Next => ???
				case Closed => throw new IllegalArgumentException("User closed window, exiting...")
			}
		} finally {
			waiter.clear()
			frame.close()
			panel.contents.clear()
			frame.dispose()
			thread.interrupt()
		}
	}
}

private[mains] object ImageSelectionPanel {
	def apply(images: Traversable[FolderImage]): FolderImage = new ImageSelectionPanel(images.toIterator).choose
}