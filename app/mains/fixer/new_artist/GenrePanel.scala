package mains.fixer.new_artist

import java.awt.{Color, Insets}

import mains.SwingUtils._
import mains.fixer.new_artist.GenrePanel.UpdatingColumns
import rx.lang.scala.{Observable, Subject}

import scala.swing.{GridBagPanel, Orientation}
import scala.swing.GridBagPanel.Anchor

import scalaz.Scalaz.{ToBindOps, ToFunctorOps, ToTraverseOps}
import scalaz.State
import common.rich.func.MoreTraverseInstances._

import common.rich.collections.RichSeq._
import common.rich.path.Directory
import common.rich.primitives.RichInt.Rich
import common.rich.RichTuple.richSameTuple2
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.RichT.lazyT
import common.rich.primitives.RichBoolean.richBoolean

// Yeah, inheritance is bad, but this is how Scala Swing rolls :\
private[fixer] class GenrePanel private(maxRows: Int, iconSideInPixels: Int, bigIconMultiplayer: Int)
    extends GridBagPanel {
  private val clickSubject = Subject[Directory]()
  def clicks: Observable[Directory] = clickSubject

  private def addSubGenres(dirs: Seq[Directory]): UpdatingColumns[Unit] = dirs
      .sortBy(_.name)
      .zipWithIndex
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

  // TODO add this in a less hacky way, since it gets messed up when the box styles change.
  private def addFilter(): UpdatingColumns[Unit] = State.get.map {usedUpColumns =>
    val filter = new GenreFilter
    filter.textChanges.subscribe(_ => applyFilter(filter.text))
    filter.choice.subscribe(_ => tryChoose(filter.text))
    filter.select.subscribe(updateSelection(filter.text)(_))

    add(filter, constraints(
      gridX = maxRows - 1,
      gridY = usedUpColumns,
      width = bigIconMultiplayer * iconSideInPixels,
      height = iconSideInPixels,
    ))
  }
  private def boxes = contents.collect {case b: GenreBox => b}
  private val defaultBackground = this.background
  private def applyFilter(s: String): Unit = {
    val totalMatches = boxes.zipWithIndex.count {case (b, i) =>
      if (s.isEmpty) {
        b.reset()
        false
      } else {
        if (b.isFuzzyMatch(s).isFalse && i == currentSelection) {
          currentSelection = -1
          b.clearSelection()
        }
        b.enableIfFuzzyMatch(s)
      }
    }
    this.background =
        if (s.isEmpty)
          defaultBackground
        else totalMatches match {
          case 0 => Color.RED
          case 1 => Color.GREEN
          case _ => Color.ORANGE
        }
  }
  private def tryChoose(s: String): Unit = boxes.filter(_.isFuzzyMatch(s)).toList match {
    case h :: Nil => clickSubject.onNext(h.directory)
    case _ if currentSelection != -1 => clickSubject.onNext(boxes(currentSelection).directory)
    case _ => ()
  }
  private var currentSelection = -1
  private def updateSelection(s: String)(d: Direction): Unit = {
    def select(newIndex: Int): Unit = {
      currentSelection = newIndex
      boxes.zipWithIndex.foreach {
        case (b, i) => if (i == currentSelection) b.select() else b.clearSelection()
      }
    }
    def firstSelection(): Unit = boxes.zipWithIndex
        .find(_._1.isFuzzyMatch(s))
        .map(_._2)
        .foreach(select)
    def update(): Unit = {
      def nextSelection(i: (Int, Int)): Option[Int] = d match {
        case Direction.Previous => i._1.onlyIf(i._2 == currentSelection)
        case Direction.Next => i._2.onlyIf(i._1 == currentSelection)
      }
      boxes.zipWithIndex.filter(_._1.isFuzzyMatch(s))
          .pairSliding
          .map(_.map(_._2))
          .mapFirst(nextSelection)
          .foreach(select)
    }
    if (currentSelection == -1) firstSelection() else update()
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
        .>>($.addFilter())
        .>|($)
        .eval(0)
  }
}