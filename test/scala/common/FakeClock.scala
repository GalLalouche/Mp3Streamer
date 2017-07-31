package common
import java.time.{Clock, Instant, ZoneId}

class FakeClock extends Clock {
  private var currentMillis = 0L
  def advance(millis: Long) = currentMillis += millis
  override def withZone(zone: ZoneId) = ???
  override def getZone = ???
  override def instant() = Instant ofEpochMilli currentMillis
}
