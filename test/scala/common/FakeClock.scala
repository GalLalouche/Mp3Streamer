package common
import org.joda.time.Instant

class FakeClock extends JodaClock {
  private var currentMillis = 0L
  def advance(millis: Long) = currentMillis += millis
  override def now = new Instant(currentMillis)
}
