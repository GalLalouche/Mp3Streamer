package mains.cover

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import scala.swing.Frame
import scala.swing.event.{ComponentAdded, WindowClosing}

/** Displays several images to the user, and returns the selected image */
private class ImageSelectionPanel private(f: Int => BlockingQueue[FolderImage]) {
  private val rows = 2
  private val cols = 3
  def choose(): ImageChoice = {
    val waiter = new LinkedBlockingQueue[ImageChoice]
    val frame = new Frame {
      reactions += { case e: WindowClosing => waiter put Cancelled }
    }
    val panel = new AsyncFolderImagePanel(cols = cols, rows = rows, imageProvider = f(rows * cols))
    panel.reactions += {
      case e: ComponentAdded => frame.pack()
      case e: ImageChoice => waiter put e
    }
    frame.contents = panel
    try {
      panel.start()
      frame.open()
      waiter.take
    } finally {
      panel.close()
      frame.dispose()
    }
  }
}

private object ImageSelectionPanel {
  def apply(f: Int => BlockingQueue[FolderImage]): ImageChoice = new ImageSelectionPanel(f).choose()
}
