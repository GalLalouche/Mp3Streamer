package mains.cover

import java.awt.{Color, Font}
import javax.swing.{BorderFactory, JLabel, SpringLayout, SwingConstants}

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import mains.SwingUtils._

import scala.concurrent.{ExecutionContext, TimeoutException}
import scala.concurrent.duration.DurationInt
import scala.swing._
import scala.util.Try

import cats.instances.future.catsStdInstancesForFuture
import common.rich.func.kats.RichOptionT.richOptionT
import common.rich.func.kats.ToMoreMonadErrorOps.toMoreApplicativeErrorOps
import common.rich.func.kats.ToMoreMonoidOps._

import common.concurrency.FutureIterant
import common.rich.RichFuture.richFutureBlocking
import common.rich.RichT._

/** Eventually publishes an ImageChoice event. */
private class AsyncFolderImagePanel @Inject() (
    ec: ExecutionContext,
    @Assisted images: FutureIterant[FolderImage],
    @Assisted("rows") rows: Int,
    @Assisted("cols") cols: Int,
) extends GridPanel(rows0 = rows, cols0 = cols) {
  private implicit val iec: ExecutionContext = ec
  import AsyncFolderImagePanel._

  private var current = images.oMap(image =>
    createImagePanel(image)
      .listenError(e => scribe.warn(s"Error converting <$image> to BufferImage", e))
      .toOption,
  )

  private def createImagePanel(fi: FolderImage): Try[Component] =
    createImageLabel(fi)
      .map(Component.wrap(_).onMouseClick(() => this.publish(ImageChoice.Selected(fi))))

  def refresh(): Unit = {
    contents.clear()
    // Pre-populate the grid to avoid images moving around.
    val range = 0 until rows * cols
    range.map("Placeholder for image #".+).map(new TextArea(_)).foreach(contents.+=)
    contents += Button("Fuck it, I'll do it myself!")(this.publish(ImageChoice.OpenBrowser))
    contents += Button("Show me more...")(refresh())
    for {
      currentIndex <- range
    } {
      val (image, next) = current.step.get.getOpt(30.seconds).getOrElse {
        this.publish(ImageChoice.ImageServerTimeout)
        throw new TimeoutException("Timed out waiting for image")
      }
      current = next
      contents.synchronized {
        contents.update(currentIndex, image)
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

  private def createImageLabel(fi: FolderImage): Try[JLabel] = Try {
    val fileSize = s"${fi.file.size / 1024}KB"
    val text = s"${fi.width}x${fi.height} $fileSize${" LOCAL".monoidFilter(fi.isLocal)}"
    new JLabel(fi.toIcon(Width, Height))
      .<|(_.setLayout(new SpringLayout()))
      .<|(_.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY)))
      .<|(TextProps.map(_.label(text)) foreach _.add)
  }
}
