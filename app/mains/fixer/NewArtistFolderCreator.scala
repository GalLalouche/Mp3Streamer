package mains.fixer

import javax.imageio.ImageIO
import mains.SwingUtils._

import scala.concurrent.{Future, Promise}
import scala.swing.{BoxPanel, Component, Frame, GridPanel, Label, Orientation}
import scala.swing.event.WindowClosing

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._

import common.rich.path.Directory
import common.rich.primitives.RichBoolean._
import common.rich.RichT._

/**
* Creates a new artist folder by asking the user which subgenre to place the artist in using a GridPanel
* view.
*/
private object NewArtistFolderCreator {
  def selectGenreDir(): Future[Directory] = {
    val promise = Promise[Directory]()

    val frame = new Frame {
      reactions += {case _: WindowClosing => promise.failure(new Exception("User closed the window"))}
    }
    def genres(genre: String) = Directory("d:/media/music/" + genre).dirs
    // TODO add jazz
    val rockDirs = genres("Rock")
    val metalDirs = genres("Metal")
    frame.contents = new GridPanel(
      rows0 = Math.max(rockDirs.length, metalDirs.length),
      cols0 = 2,
    ) {
      private def genreComponent(d: Option[Directory]): Component = d.mapHeadOrElse(
        d => new BoxPanel(Orientation.Horizontal) {
          val folderImage = d.\("folder.jpg").mapIf(_.exists().isFalse).to(d / "folder.png")
          contents += ImageIO.read(folderImage).toSquareImageIcon(side = 35).toComponent
          contents += new Label(d.name)
        }.onMouseClick {() =>
          frame.dispose()
          promise.success(d)
        },
        new Label(""),
      )
      for {
        (d1, d2) <- rockDirs.zipAll(metalDirs, null, null) // Ensures correct order in the grid.
        d <- Vector(d1, d2)
      } contents += genreComponent(Option(d))
    }
    frame.open()
    promise.future
  }
}
