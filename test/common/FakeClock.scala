package common

import java.time.{Clock, Instant, ZoneId, ZoneOffset}

class FakeClock extends Clock {
  private var currentMillis = 0L
  def advance(millis: Long): Unit = currentMillis += millis
  override def withZone(zone: ZoneId) = ???
  override def getZone: ZoneId = ZoneOffset.UTC
  override def instant() = Instant.ofEpochMilli(currentMillis)
}
