package mains.fixer

import javax.imageio.ImageIO

import common.rich.path.Directory
import mains.SwingUtils

import scala.concurrent.{Future, Promise}
import scala.swing.event.WindowClosing
import scala.swing.{BoxPanel, Component, Frame, GridPanel, Label, Orientation}

private object NewArtistFolderCreator extends SwingUtils {
  private val NumberOfLabelRows = 1
  def apply(artistName: String): Future[Directory] = {
    val promise = Promise[Directory]()
    val frame = new Frame {
      reactions += {case _: WindowClosing => promise.failure(new Exception("User closed the window"))}
    }
    def genres(genre: String) = Directory("d:/media/music/" + genre).dirs
    val rockDirs = genres("Rock")
    val metalDirs = genres("Metal")
    frame.contents = new GridPanel(rows0 = Math.max(rockDirs.length, metalDirs.length) + NumberOfLabelRows, cols0 = 2) {
      private def genreComponent(d: Directory): Component =
        if (d == null) new Label("") else
          new BoxPanel(Orientation.Horizontal) {
            contents += ImageIO.read(d / "folder.jpg").toSquareImageIcon(40).toComponent
            contents += new Label(d.name)
          }.onMouseClick(() => {
            frame.dispose()
            promise.success(d addSubDir artistName)
          })
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
