package mains.fixer

import mains.fixer.new_artist.GenrePanel

import scala.concurrent.{Future, Promise}
import scala.swing.Frame
import scala.swing.event.WindowClosing

import common.rich.path.Directory

/**
* Creates a new artist folder by asking the user which subgenre to place the artist in using a GridPanel
* view.
*
* Should look something like this
*
* ------
* Rock           || Metal  || Jazz || New Age
* Folk  Pop      || Black
* Hard  Classic  || Death
* Prog           || Doom
*/
private object NewArtistFolderCreator {
  def selectGenreDir(): Future[Directory] = {
    def genre(dirName: String) = Directory("d:/media/music/" + dirName)
    val panel = GenrePanel(
      maxRows = MaxRows, iconSideInPixels = IconSideInPixels, bigIconMultiplayer = BigIconMultiplayer,
      subGenreDirs = Seq("Rock", "Metal").map(genre(_).dirs),
      bigGenreDirs = Seq("Jazz", "New Age", "Musicals").map(genre),
    )

    val $ = Promise[Directory]()
    val frame = new Frame {
      reactions += {case _: WindowClosing => $.failure(new Exception("User closed the window"))}
    }
    frame.contents = panel
    frame.open()
    panel.clicks.first.subscribe {d =>
      $.success(d)
      frame.dispose()
    }
    $.future
  }

  private val MaxHeightInPixels = 800
  private val IconSideInPixels = 50
  private val BigIconMultiplayer = 3
  private val MaxRows = MaxHeightInPixels / IconSideInPixels
}
