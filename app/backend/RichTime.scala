package backend
import java.time._

object RichTime {
  implicit val durationOrdering: Duration => Ordering[Duration]#Ops = implicitly[Ordering[Duration]].mkOrderingOps _
  private implicit object OrderingLocalDateTime extends Ordering[LocalDateTime] {
    override def compare(x: LocalDateTime, y: LocalDateTime) = x.toMillis compareTo y.toMillis
  }
  implicit val LocalDateTimeOrdering:
    (LocalDateTime) => Ordering[LocalDateTime]#Ops = OrderingLocalDateTime.mkOrderingOps

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
    def toLocalDateTime: LocalDateTime = $.instant.atZone(ZoneId.systemDefault).toLocalDateTime
  }
}
