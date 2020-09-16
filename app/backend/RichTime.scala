package backend

import java.time._

import scala.math.Ordered.orderingToOrdered

import common.rich.primitives.RichBoolean.richBoolean

object RichTime {
  implicit val OrderingLocalDateTime: Ordering[LocalDateTime] = Ordering.by(_.toMillis)
  implicit val OrderingLocalDate: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  implicit class RichInstant($: Instant) {
    def toLocalDateTime: LocalDateTime = LocalDateTime from $.atZone(ZoneId.systemDefault)
  }
  implicit class RichLong($: Long) {
    def toLocalDateTime: LocalDateTime = Instant.ofEpochMilli($).toLocalDateTime
  }
  implicit class RichClock($: Clock) {
    def getLocalDateTime: LocalDateTime = $.instant.atZone(ZoneId.systemDefault).toLocalDateTime
  }
  implicit class RichLocalDateTime($: LocalDateTime) {
    def toMillis: Long = $.atZone(ZoneId.systemDefault).toInstant.toEpochMilli
    def age(c: Clock): Duration = Duration.between($, c.getLocalDateTime)
    def isNewerThan(d: Duration, c: Clock): Boolean = age(c) < d
    def isOlderThan(d: Duration, c: Clock): Boolean = isNewerThan(d, c).isFalse
  }
}
