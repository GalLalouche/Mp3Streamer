package common

import java.util.Date

import common.rich.func.MoreMonadPlus._
import common.rich.func.RichMonadPlus._
import common.rich.primitives.RichOption._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, DateTimeZone}


/** Tries several parsers in a sequence until the first one succeeds. Isn't total. */
class CompositeDateFormat private(formatters: Seq[DateTimeFormatter]) {
  require(formatters.nonEmpty)

  def parse(source: String): Option[DateTime] = formatters
      .tryMap(_.parseDateTime(source))
      .headOption

  def print(date: Date): String = print(date.getTime)
  def print(date: DateTime): String = print(date.getMillis)
  def print(time: Long): String = formatters.head.print(time)
}

object CompositeDateFormat {
  def apply(patterns: String*) = new CompositeDateFormat(patterns
      .map(DateTimeFormat.forPattern)
      .map(_.withZone(DateTimeZone.UTC))
  )
}

