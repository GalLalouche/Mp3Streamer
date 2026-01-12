package mains.fixer

import com.google.inject.Inject
import mains.BrowserUtils
import mains.fixer.NewArtistFolderCreator.{BigIconMultiplayer, IconSideInPixels, MaxRows}
import mains.fixer.new_artist.GenrePanel
import models.TypeAliases.ArtistName
import musicfinder.IOMusicFinder

import scala.concurrent.{Future, Promise}
import scala.swing.Frame
import scala.swing.event.WindowClosing

import common.rich.path.Directory
import common.rich.primitives.RichOption.richOption

/**
 * Creates a new artist folder by asking the user which subgenre to place the artist in using a
 * GridPanel view.
 *
 * Should look something like this
 *
 * {{{
 * ------
 * Rock           || Metal         || Jazz || New Age
 * Folk | Pop     || Black | Death
 * Hard | Classic || Death | Prog
 *                || Doom
 * }}}
 */
private class NewArtistFolderCreator @Inject() (
    mf: IOMusicFinder,
) {
  def selectGenreDirAndPopupBrowser(name: ArtistName): Future[Directory] = {
    BrowserUtils.searchForLucky(name + " rateyourmusic")
    selectGenreDir(name)
  }
  def selectGenreDir(name: ArtistName): Future[Directory] = {
    def genre(dirName: String): Directory =
      mf.baseDir.getDir(dirName).getOrThrow(s"Could not find genre dir <$dirName>").dir
    val panel = GenrePanel(
      maxRows = MaxRows,
      iconSideInPixels = IconSideInPixels,
      bigIconMultiplayer = BigIconMultiplayer,
      subGenreDirs = Vector("Rock", "Metal").map(genre(_).dirs.toVector),
      // Classical isn't here since classical music has its own special ordering mechanism.
      bigGenreDirs = Vector("Blues", "Jazz", "New Age", "Musicals").map(genre),
    )

    val $ = Promise[Directory]()
    val frame = new Frame {
      reactions += { case _: WindowClosing => $.failure(new Exception("User closed the window")) }
    }
    frame.contents = panel
    frame.open()
    frame.title = "Select genre for " + name
    panel.clicks.first.subscribe { d =>
      $.success(d)
      frame.dispose()
    }
    $.future
  }
}

private object NewArtistFolderCreator {
  private val MaxHeightInPixels = 800
  private val IconSideInPixels = 50
  private val BigIconMultiplayer = 3
  private val MaxRows = MaxHeightInPixels / IconSideInPixels
}
