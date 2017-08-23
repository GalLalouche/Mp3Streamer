package common

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util.Date

import backend.RichTime._
import common.rich.func.MoreMonadPlus._
import common.rich.func.ToMoreMonadPlusOps

/** Tries several parsers in a sequence until the first one succeeds. Isn't total. */
class CompositeDateFormat private(formatters: Seq[DateTimeFormatter]) extends ToMoreMonadPlusOps {
  require(formatters.nonEmpty)

  def parse(source: String): Option[LocalDateTime] = formatters
      .tryMap(LocalDateTime.parse(source, _))
      .headOption

  def print(date: Date): String = print(date.getTime)
  def print(date: LocalDateTime): String = formatters.head.format(date)
  def print(time: Long): String = print(time.toLocalDateTime)
}

object CompositeDateFormat {
  def apply(patterns: String*) = new CompositeDateFormat(patterns
      .map(DateTimeFormatter.ofPattern)
      .map(_.withZone(ZoneId.systemDefault)))
}

