package backend

import java.time._

object RichTime {
  implicit val OrderingLocalDateTime: Ordering[LocalDateTime] = Ordering.by(_.toMillis)
  implicit val OrderingLocalDate: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  implicit class RichInstant($: Instant) {
    def toLocalDateTime: LocalDateTime = LocalDateTime from $.atZone(ZoneId.systemDefault)
  }
  implicit class RichLong($: Long) {
    def toLocalDateTime: LocalDateTime = Instant.ofEpochMilli($).toLocalDateTime
  }
  implicit class RichLocalDateTime($: LocalDateTime) {
    def toMillis: Long = $.atZone(ZoneId.systemDefault).toInstant.toEpochMilli
  }
  implicit class RichClock($: Clock) {
    def getLocalDateTime: LocalDateTime = $.instant.atZone(ZoneId.systemDefault).toLocalDateTime
  }
}
