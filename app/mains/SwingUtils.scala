package mains

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.image.BufferedImage
import java.awt.{Image, RenderingHints}
import javax.swing.{ImageIcon, JLabel}

import scala.swing.Component

private object SwingUtils {
  implicit class RichComponent($: Component) {
    // Because reactions is BS and doesn't work.
    def onMouseClick(f: () => Any): Component = {
      $.peer.addMouseListener(new MouseListener {
        override def mouseExited(e: MouseEvent): Unit = ()
        override def mousePressed(e: MouseEvent): Unit = ()
        override def mouseReleased(e: MouseEvent): Unit = ()
        override def mouseEntered(e: MouseEvent): Unit = ()
        override def mouseClicked(e: MouseEvent): Unit = f()
      })
      $
    }
  }
  implicit class RichImage(image: Image) {
    def toSquareImageIcon(side: Int): ImageIcon = toImageIcon(side, side)
    def toImageIcon(height: Int, width: Int): ImageIcon = {
      val bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      val graphics = bufferedImage.createGraphics
      graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      graphics.drawImage(image, 0, 0, width, height, null)
      graphics.dispose()
      new ImageIcon(bufferedImage)
    }
  }
  implicit class RichImageIcon($: ImageIcon) {
    def toComponent: Component = Component wrap new JLabel($)
  }
}
