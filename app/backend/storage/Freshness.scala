package backend.storage

import java.time.LocalDateTime

sealed trait Freshness {
  def localDateTime: Option[LocalDateTime]
}

// For values with no expiration.
case object AlwaysFresh extends Freshness {
  override val localDateTime: Option[LocalDateTime] = None
}
case class DatedFreshness(date: LocalDateTime) extends Freshness {
  override val localDateTime = Some(date)
}

