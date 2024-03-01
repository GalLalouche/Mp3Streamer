package mains.fixer

import mains.BrowserUtils
import mains.fixer.new_artist.GenrePanel
import models.TypeAliases.ArtistName

import scala.concurrent.{Future, Promise}
import scala.swing.Frame
import scala.swing.event.WindowClosing

import common.rich.path.Directory

/**
 * Creates a new artist folder by asking the user which subgenre to place the artist in using a
 * GridPanel view.
 *
 * Should look something like this
 *
 * ------ Rock || Metal || Jazz || New Age Folk Pop || Black Hard Classic || Death Prog || Doom
 */
private object NewArtistFolderCreator {
  def selectGenreDirAndPopupBrowser(name: ArtistName): Future[Directory] = {
    BrowserUtils.searchForLucky(name + " rateyourmusic")
    selectGenreDir(name)
  }
  def selectGenreDir(name: ArtistName): Future[Directory] = {
    // TODO this should use IOMusicFinder
    def genre(dirName: String) = Directory("g:/media/music/" + dirName)
    val panel = GenrePanel(
      maxRows = MaxRows,
      iconSideInPixels = IconSideInPixels,
      bigIconMultiplayer = BigIconMultiplayer,
      subGenreDirs = Seq("Rock", "Metal").map(genre(_).dirs),
      // Classical isn't here since classical music has its own special ordering mechanism.
      bigGenreDirs = Seq("Blues", "Jazz", "New Age", "Musicals").map(genre),
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

  private val MaxHeightInPixels = 800
  private val IconSideInPixels = 50
  private val BigIconMultiplayer = 3
  private val MaxRows = MaxHeightInPixels / IconSideInPixels
}
