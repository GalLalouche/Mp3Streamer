package common
import org.joda.time.Instant

// Since joda time has no clock
trait JodaClock {
  def now: Instant
}

object JodaClock {
  private object RealJodaClock extends JodaClock {
    override def now: Instant = Instant.now
  }
  def real: JodaClock = RealJodaClock
}
