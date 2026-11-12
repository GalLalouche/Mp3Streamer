package common

import java.time._

import com.google.inject.Singleton

@Singleton class FakeClock extends Clock {
  def this(time: LocalDateTime) = {
    this()
    currentMillis = time.toInstant(getZone).toEpochMilli
  }
  private var currentMillis = 0L
  def advance(millis: Long): Unit = currentMillis += millis
  override def withZone(zone: ZoneId) = ???
  override def getZone: ZoneOffset = ZoneOffset.UTC
  override def instant() = Instant.ofEpochMilli(currentMillis)
}
