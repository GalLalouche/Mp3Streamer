package mains.fixer

import java.awt.RenderingHints
import java.awt.event.{MouseEvent, MouseListener}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, JLabel}

import common.rich.path.Directory

import scala.concurrent.{Future, Promise}
import scala.swing.event.WindowClosing
import scala.swing.{BoxPanel, Component, Frame, GridPanel, Label, Orientation}

private object NewArtistFolderCreator {
  def apply(artistName: String): Future[Directory] = {
    val promise = Promise[Directory]()
    val frame = new Frame {
      reactions += {case _: WindowClosing => promise.failure(new Exception("User closed the window"))}
    }
    def genres(genre: String) = Directory("d:/media/music/" + genre).dirs
    val rockDirs = genres("Rock")
    val metalDirs = genres("Metal")
    val numberOfColumns = Math.max(rockDirs.length, metalDirs.length) + 1 // +1 for labels
    frame.contents = new GridPanel(numberOfColumns, 2) {
      private def genreComponent(d: Directory): Component =
        if (d == null) new Label("") else {
          val $ = new BoxPanel(Orientation.Horizontal) {
            contents += {
              // TODO remove code duplications with AsyncFolderImagePanel
              val image = ImageIO.read(d / "folder.jpg")
              val dimensions = 40
              val $ = new BufferedImage(dimensions, dimensions, BufferedImage.TYPE_INT_ARGB)
              val graphics = $.createGraphics
              graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
              graphics.drawImage(image, 0, 0, dimensions, dimensions, null)
              graphics.dispose()
              Component wrap new JLabel(new ImageIcon($))
            }
            contents += new Label(d.name)
          }
          // TODO remove code duplication
          $.peer.addMouseListener(new MouseListener {
            override def mouseExited(e: MouseEvent): Unit = ()
            override def mousePressed(e: MouseEvent): Unit = ()
            override def mouseReleased(e: MouseEvent): Unit = ()
            override def mouseEntered(e: MouseEvent): Unit = ()
            override def mouseClicked(e: MouseEvent): Unit = {
              frame.dispose()
              promise.success(d addSubDir artistName)
            }
          })
          $
        }
      contents += new Label("Rock")
      contents += new Label("Metal")
      // ensures correct order in the grid
      for ((d1, d2) <- rockDirs.zipAll(metalDirs, null, null)) {
        contents += genreComponent(d1)
        contents += genreComponent(d2)
      }
    }
    frame.open()
    promise.future
  }
}
