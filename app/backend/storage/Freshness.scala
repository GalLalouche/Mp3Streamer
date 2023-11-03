package backend.storage

import java.time.LocalDateTime

import scalaz.std.option.optionInstance
import common.rich.func.ToMoreFoldableOps._
import monocle.Iso

sealed trait Freshness {
  def localDateTime: Option[LocalDateTime]
}
object Freshness {
  def iso =
    Iso[Option[LocalDateTime], Freshness](_.mapHeadOrElse(DatedFreshness, AlwaysFresh))(
      _.localDateTime,
    )
}

// For values with no expiration.
case object AlwaysFresh extends Freshness {
  override val localDateTime: Option[LocalDateTime] = None
}
case class DatedFreshness(date: LocalDateTime) extends Freshness {
  override val localDateTime = Some(date)
}
