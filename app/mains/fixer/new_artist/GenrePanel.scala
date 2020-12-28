package mains.fixer.new_artist

import java.awt.Insets

import mains.SwingUtils._
import mains.fixer.new_artist.GenrePanel.UpdatingColumns
import rx.lang.scala.{Observable, Subject}

import scala.swing.{GridBagPanel, Orientation}
import scala.swing.GridBagPanel.Anchor

import scalaz.Scalaz.{ToBindOps, ToFunctorOps, ToTraverseOps}
import scalaz.State
import common.rich.func.MoreTraverseInstances._

import common.rich.path.Directory
import common.rich.primitives.RichInt.Rich

// Yeah, inheritance is bad, but this is how Scala Swing rolls :\
private[fixer] class GenrePanel private(maxRows: Int, iconSideInPixels: Int, bigIconMultiplayer: Int)
    extends GridBagPanel {
  private val clickSubject = Subject[Directory]()
  def clicks: Observable[Directory] = clickSubject

  private def addSubGenres(dirs: Seq[Directory]): UpdatingColumns[Unit] = dirs.zipWithIndex
      .traverse {case (dir, index) =>
        dynamicConstraint(relativeGenreIndex = index, width = 1, height = 1).map(
          add(
            genreBox(dir, Orientation.Horizontal, sideMultiplayer = 1, fontSize = None),
            _,
          ))
      }
      .>>(State.modify(_ + (dirs.length ceilDiv maxRows)))

  private def addBigSizeIcon(dir: Directory): UpdatingColumns[Unit] =
    dynamicConstraint(relativeGenreIndex = 0, width = bigIconMultiplayer, height = bigIconMultiplayer + 1)
        .map(
          add(
            genreBox(dir, Orientation.Vertical, sideMultiplayer = bigIconMultiplayer, fontSize = Some(35)),
            _,
          )
        ).>>(State.modify[Int](_ + bigIconMultiplayer))

  private def addFilter: UpdatingColumns[Unit] = State.get.map {usedUpColumns =>
    val filter = new GenreFilter
    filter.text.subscribe(applyFilter _)
    filter.select.subscribe(trySelect _)

    add(filter, constraints(
      gridX = maxRows - 1,
      gridY = usedUpColumns,
      width = bigIconMultiplayer * iconSideInPixels,
      height = iconSideInPixels,
    ))
  }
  private def boxes = contents.collect {case b: GenreBox => b}
  private def applyFilter(s: String): Unit =
    boxes.foreach(b => if (s.isEmpty) b.reset() else b.enableIfFuzzyMatch(s))
  private def trySelect(s: String): Unit = boxes.filter(_.isFuzzyMatch(s)).toList match {
    case h :: Nil => clickSubject.onNext(h.directory)
    case _ => ()
  }

  private def genreBox(
      d: Directory, orientation: Orientation.Value, sideMultiplayer: Int, fontSize: Option[Int]
  ) = new GenreBox(d, orientation, fontSize, iconSideInPixels * sideMultiplayer)
      .onMouseClick {() => clickSubject.onNext(d)}
  private def dynamicConstraint(
      relativeGenreIndex: Int, width: Int, height: Int): UpdatingColumns[Constraints] =
    State.get.map(usedUpColumns => constraints(
      gridX = usedUpColumns + relativeGenreIndex / maxRows,
      gridY = relativeGenreIndex % maxRows,
      width = width,
      height = height,
    ))
  private def constraints(gridX: Int, gridY: Int, width: Int, height: Int): Constraints = new Constraints(
    gridx = gridX,
    gridy = gridY,
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

object GenrePanel {
  private type UpdatingColumns[A] = State[Int, A]
  def apply(
      maxRows: Int, iconSideInPixels: Int, bigIconMultiplayer: Int,
      subGenreDirs: Seq[Seq[Directory]], bigGenreDirs: Seq[Directory],
  ): GenrePanel = {
    val $ = new GenrePanel(
      maxRows = maxRows, iconSideInPixels = iconSideInPixels, bigIconMultiplayer = bigIconMultiplayer)
    subGenreDirs.traverse($.addSubGenres)
        .>>(bigGenreDirs.traverse($.addBigSizeIcon))
        .>>($.addFilter)
        .>|($)
        .eval(0)
  }
}