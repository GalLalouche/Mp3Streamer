package mains.vimtag.table

import java.util.regex.Pattern

import mains.vimtag.{IndividualId3, IndividualParser}

import scalaz.std.vector.vectorInstance
import scalaz.State
import scalaz.syntax.bind.ToBindOps
import scalaz.syntax.functor.ToFunctorOps
import scalaz.syntax.traverse._

import common.rich.primitives.RichString._
import common.rich.RichT._
import common.rich.primitives.RichBoolean._

private object TableParser extends IndividualParser {
  private type CurrentDiscNumber = Option[String]

  // "X -> In" disc number means all entries from this point until the next explicit disc number should be X.
  private def individual(table: Seq[String]): State[CurrentDiscNumber, IndividualId3] = {
    val Seq(trackNumber, title, discNumber, file) = table
    lazy val updateDiscNumber: State[CurrentDiscNumber, CurrentDiscNumber] =
      discNumber.optFilter(_.nonEmpty) match {
        case None => State.get
        case Some(v) if v.endsWith("->") => State.put(Option(v.dropRight(2))) >> State.get
        case Some(v) => State.put[CurrentDiscNumber](None) >| Option(v)
      }
    def throwOnEmpty(tag: String, s: String) =
      s.ifNot(_.nonEmpty).thenThrow(new NoSuchElementException(s"key not found for $file: $tag"))
    updateDiscNumber.map(IndividualId3(
      throwOnEmpty("FILE", file),
      throwOnEmpty("TITLE", title),
      trackNumber.toInt,
      _
    ))
  }

  private val LineRegex = Pattern compile """^\| *\d{1,3}.*""" // e.g., "| 1"
  override def cutoff = _.startsWith("|-").isFalse
  override def apply(xs: Seq[String]) = xs
      .filter(_.matches(LineRegex))
      .map(_
          .split(""" ?\| ?""")
          .toVector
          .tail // The head is always empty.
          .map(_.trim))
      .toVector
      .traverse(individual)
      .eval(None)
}
