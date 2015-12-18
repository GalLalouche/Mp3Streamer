package mains.cover

import java.io.File
import scala.swing.event.WindowClosing
import scala.swing.event.MouseClicked
import scala.swing.GridPanel
import scala.swing.Frame
import java.util.concurrent.LinkedBlockingQueue
import scala.swing.Label
import java.util.concurrent.BlockingQueue
import scala.swing.event.ComponentAdded
import scala.swing.Dialog

/**
  * Displays several images to the user, and returns the selected image
  */
private class ImageSelectionPanel private (f: Int => BlockingQueue[FolderImage]) {
	import ImageSelectionPanel._
	private val rows = 2
	private val cols = 3
	def choose(): FolderImage = {
		val waiter = new LinkedBlockingQueue[Choice]
		val frame = new Frame {
			reactions += { case e: WindowClosing => waiter put Closed }
		}
		val panel = new AsyncFolderImagePanel(cols = cols, rows = rows, imageProvider = f(rows * cols))
		panel.reactions += {
			case e: ComponentAdded => frame.pack()
			case AsyncFolderImagePanel.ImageClicked(f) => waiter put Selected(f)
		}
		frame.contents = panel
		try {
			panel.start()
			frame.open()
			waiter.take match {
				case Selected(f) => return f
				case Next => ???
				case Closed => throw new IllegalArgumentException("User closed window, exiting...")
			}
		} finally {
			panel.close()
			frame.dispose()
		}
	}
}

private object ImageSelectionPanel {
	private abstract sealed class Choice
	private case object Next extends Choice
	private case class Selected(val i: FolderImage) extends Choice
	private case object Closed extends Choice
	def apply(f: Int => BlockingQueue[FolderImage]): FolderImage = new ImageSelectionPanel(f).choose()
}