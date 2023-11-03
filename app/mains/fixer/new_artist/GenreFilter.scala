package mains.fixer.new_artist

import java.awt.Dimension
import scala.swing.{BoxPanel, Label, Orientation, Panel, TextField}
import scala.swing.event.{Key, KeyReleased, ValueChanged}

import common.rich.RichT.richT
import mains.SwingUtils._
import rx.lang.scala.{Observable, Subject}

private class GenreFilter extends Panel {
  private val textSubject = Subject[Unit]()
  def textChanges: Observable[Unit] = textSubject
  private val chooseSubject = Subject[Unit]()
  def choice: Observable[Unit] = chooseSubject
  private val selectSubject = Subject[Direction]()
  def select: Observable[Direction] = selectSubject

  private val textBox = new TextField(10).setFontSize(20).<|(_.requestFocus())
  def text: String = textBox.text
  textBox.reactions += { case _: ValueChanged =>
    textSubject.onNext(())
  }
  textBox.keys.reactions += { case KeyReleased(_, key, _, _) =>
    key match {
      case Key.Enter => chooseSubject.onNext(())
      case Key.Up => selectSubject.onNext(Direction.Previous)
      case Key.Down => selectSubject.onNext(Direction.Next)
      case _ => ()
    }
  }
  private val selectLabel = new Label("")
  val box = {
    val box = new BoxPanel(Orientation.Vertical)
    box.contents += selectLabel
    // I'm sure there's a great reason why one needs to change the alignment of the textbox to control the
    // alignment of the label above it :\
    box.contents += textBox.<|(_.minimumSize = new Dimension(165, 40)).<|(_.xLayoutAlignment = 0)
    box
  }
  _contents += box
  def display(genreName: String): Unit = selectLabel.text = genreName.ensuring(_.nonEmpty)
  def undisplay(): Unit = selectLabel.text = ""
}
