package mains.cover

import java.util.concurrent.Semaphore

import scala.concurrent.ExecutionContext
import scala.swing.event.MouseClicked
import scala.swing.{Button, GridPanel, Label, TextArea}

/** MUTANT TEENAGE NINJA TURTLES :D */
private class AsyncFolderImagePanel(rows: Int, cols: Int, imagesSupplier: ImagesSupplier)(implicit ec: ExecutionContext)
    extends GridPanel(rows0 = rows, cols0 = cols) with AutoCloseable {
  val openBrowserButton: Button = Button.apply("Fuck it, I'll do it myself!") {
    AsyncFolderImagePanel.this.publish(OpenBrowser)
    this.close()
  }
  private val thread = new DelayedThread("Image placer")
  private val waitForNextClick = new Semaphore(0)
  private var realSize = 0
  def start() {
    def createImagePanel(image: FolderImage) =
      new Label {
        icon = image.imageIcon
        listenTo(mouse.clicks)
        reactions += {
          case e: MouseClicked => AsyncFolderImagePanel.this.publish(Selected(image))
        }
      }
    thread.start(
      () => {
        if (contents.isEmpty) {
          realSize = 0
          (0 until rows * cols).foreach(i => contents += new TextArea("Placeholder for image #" + i))
          contents += openBrowserButton
          contents += Button.apply("Show me more")(waitForNextClick.release())
          Thread sleep 100
        }
        else if (realSize < rows * cols) {
          val currentIndex = realSize
          imagesSupplier.next().map(createImagePanel).foreach{e =>
            contents.synchronized {
              contents.update(currentIndex, e)
            }
          }
          realSize += 1
        }
        else {
          // if done, wait for release
          waitForNextClick.acquire()
          contents.clear()
        }
      }
    )
  }

  override def close() = {
    thread.close()
    contents.clear()
    super.repaint()
    visible = false
  }
}
