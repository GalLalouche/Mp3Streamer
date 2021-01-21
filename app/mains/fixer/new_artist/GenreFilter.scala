package mains.fixer.new_artist

import mains.SwingUtils._
import rx.lang.scala.{Observable, Subject}

import scala.swing.{Panel, TextField}
import scala.swing.event.{Key, KeyReleased, ValueChanged}

import common.rich.RichT.richT

private class GenreFilter extends Panel {
  private val textSubject = Subject[Unit]()
  def textChanges: Observable[Unit] = textSubject
  private val chooseSubject = Subject[Unit]()
  def choice: Observable[Unit] = chooseSubject

  private val tf = new TextField(10).setFontSize(20).<|(_.requestFocus())
  def text: String = tf.text
  tf.reactions += {
    case _: ValueChanged => textSubject.onNext(())
  }
  tf.keys.reactions += {
    case KeyReleased(_, key, _, _) if key == Key.Enter => chooseSubject.onNext(())
  }
  _contents += tf
}
