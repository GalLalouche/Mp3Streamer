package common

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, Year, YearMonth}

import common.CompositeDateFormat.Parser

import scala.util.Try

object CompositeDateFormat {
  private def toParser[T](pattern: String)(implicit ev: LocalDateTimeable[T]): Parser = source =>
    Try(ev.parse(source, DateTimeFormatter ofPattern pattern)).map(ev.toLocalDateTime).toOption
  private type Parser = String => Option[LocalDateTime]
  def apply[T: LocalDateTimeable](parser: String): CompositeDateFormat =
    new CompositeDateFormat(Vector.empty) orElse parser
  trait LocalDateTimeable[T] {
    def toLocalDateTime(t: T): LocalDateTime
    val parse: (String, DateTimeFormatter) => T
  }
  object LocalDateTimeable {
    implicit object LocalDateTimeLocalDateTimeable extends LocalDateTimeable[LocalDateTime] {
      override def toLocalDateTime(t: LocalDateTime) = t
      // Haskell ;(
      override val parse: (CharSequence, DateTimeFormatter) => LocalDateTime = LocalDateTime.parse
    }
    implicit object LocalDateLocalDateTimeable extends LocalDateTimeable[LocalDate] {
      override def toLocalDateTime(t: LocalDate) = t.atStartOfDay
      override val parse: (CharSequence, DateTimeFormatter) => LocalDate = LocalDate.parse
    }
    implicit object YearMonthLocalDateTimeable extends LocalDateTimeable[YearMonth] {
      override def toLocalDateTime(t: YearMonth) =
        LocalDateLocalDateTimeable toLocalDateTime t.atDay(1)
      override val parse: (CharSequence, DateTimeFormatter) => YearMonth = YearMonth.parse
    }
    implicit object YearLocalDateTimeable extends LocalDateTimeable[Year] {
      override def toLocalDateTime(t: Year) =
        YearMonthLocalDateTimeable toLocalDateTime t.atMonth(1)
      override val parse: (CharSequence, DateTimeFormatter) => Year = Year.parse
    }
  }
}


/** Tries several parsers in a sequence until the first one succeeds. Isn't total. */
class CompositeDateFormat private(parsers: Seq[Parser]) {
  def parse(source: String): Option[LocalDateTime] = parsers.toStream.flatMap(_ (source)).headOption
  def orElse[T: CompositeDateFormat.LocalDateTimeable](parser: String): CompositeDateFormat =
    new CompositeDateFormat(parsers :+ CompositeDateFormat.toParser(parser))
}

