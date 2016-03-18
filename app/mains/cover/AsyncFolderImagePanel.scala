package mains.cover

import java.util.concurrent.BlockingQueue

import scala.concurrent.Lock
import scala.swing.event.MouseClicked
import scala.swing.{Button, GridPanel, Label}

/** MUTANT TEENAGE NINJA TURTLES :D */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imageProvider: BlockingQueue[FolderImage])
  extends GridPanel(rows0 = rows, cols0 = cols) with AutoCloseable {
  private val thread = new DelayedThread("Image placer")
  private val waitForNextClick = new Lock
  waitForNextClick.available = false
  def start() {
    def createImagePanel(image: FolderImage) =
      new Label {
        icon = image.imageIcon
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked => AsyncFolderImagePanel.this.publish(Selected(image))
        }
      }
    thread.start(() => {
      if (contents.isEmpty)
        contents += Button.apply("Fuck it, I'll do it myself!") {
          AsyncFolderImagePanel.this.publish(OpenBrowser)
          thread.close()
        }
      else if (contents.size < rows * cols)
        contents += createImagePanel(imageProvider.take())
      else {
        // if done, add two buttons
        contents += Button.apply("Show me more")(waitForNextClick.release())
        waitForNextClick.acquire()
        contents.clear()
      }
    })
  }

  override def close() = {
    thread.close()
    contents.clear()
  }
}
