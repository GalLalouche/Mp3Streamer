package mains.fixer

import java.awt.Insets
import java.io.File

import javax.imageio.ImageIO
import mains.SwingUtils._
import rx.lang.scala.{Observable, Subject}

import scala.swing.{BoxPanel, Component, GridBagPanel, Label, Orientation}
import scala.swing.GridBagPanel.Anchor

import common.rich.RichT._
import common.rich.path.Directory
import common.rich.primitives.RichInt.Rich

// TODO replace mutating methods with object factory.
// Yeah, inheritance is bad, but this is how Scala Swing rolls :\
private class GenrePanel(maxRows: Int, iconSideInPixels: Int, bigIconMultiplayer: Int) extends GridBagPanel {
  private val clickSubject = Subject[Directory]()
  def clicks: Observable[Directory] = clickSubject

  private var usedUpColumns: Int = 0 // I mean, it's stateful anyway :\
  def addSubGenres(dirs: Seq[Directory]): Unit = {
    for ((dir, index) <- dirs.zipWithIndex)
      add(
        component(dir, Orientation.Horizontal, sideMultiplayer = 1, fontSize = None),
        constraints(relativeGenreIndex = index, width = 1, height = 1),
      )
    usedUpColumns += dirs.length ceilDiv maxRows
  }

  def addBigSizeIcon(dir: Directory): Unit = {
    add(
      component(dir, Orientation.Vertical, sideMultiplayer = bigIconMultiplayer, fontSize = Some(35)),
      constraints(relativeGenreIndex = 0, width = bigIconMultiplayer, height = bigIconMultiplayer + 1),
    )
    usedUpColumns += bigIconMultiplayer
  }

  private def component(
      d: Directory, orientation: Orientation.Value, sideMultiplayer: Int, fontSize: Option[Int]
  ): Component = new BoxPanel(orientation) {
    val folderImage: File = d.\("folder.jpg").optFilter(_.exists).getOrElse(d / "folder.png")
    contents += ImageIO.read(folderImage)
        .toSquareImageIcon(iconSideInPixels * sideMultiplayer)
        .toComponent
    contents += new Label(d.name).joinOption(fontSize)(_ setFontSize _)
  }.onMouseClick {() => clickSubject.onNext(d)}
  private def constraints(relativeGenreIndex: Int, width: Int, height: Int) =
    new Constraints(
      gridx = usedUpColumns + relativeGenreIndex / maxRows,
      gridy = relativeGenreIndex % maxRows,
      gridwidth = width,
      gridheight = height,
      weightx = 0,
      weighty = 0,
      anchor = Anchor.FirstLineStart.id,
      fill = 0,
      insets = new Insets(0, 0, 0, 0),
      ipadx = 5,
      ipady = 1,
    )
}
