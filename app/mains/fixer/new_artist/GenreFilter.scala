package mains.fixer.new_artist

import mains.SwingUtils._
import rx.lang.scala.{Observable, Subject}

import scala.swing.{Panel, TextField}
import scala.swing.event.{Key, KeyReleased, ValueChanged}

import common.rich.RichT.richT

private class GenreFilter extends Panel {
  private val textSubject = Subject[String]()
  /** Filter text changes. */
  def text: Observable[String] = textSubject
  private val selectSubject = Subject[String]()
  /** 'Enter' clicks; publishes the value of the filter text. */
  def select: Observable[String] = selectSubject

  private val tf = new TextField(10).setFontSize(20).<|(_.requestFocus())
  tf.reactions += {
    case _: ValueChanged => textSubject.onNext(tf.text)
  }
  tf.keys.reactions += {
    case KeyReleased(_, key, _, _) if key == Key.Enter => selectSubject.onNext(tf.text)
  }
  _contents += tf
}
