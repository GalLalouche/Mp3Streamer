package common
import java.time.LocalDateTime

object RichTime {
  implicit class RichLocalDateTime(l: LocalDateTime) {
    def toMillis: Long = ???
  }
  implicit class RichLong(l: Long) {
    def toLocalDateTime: LocalDateTime = ???
  }
}
