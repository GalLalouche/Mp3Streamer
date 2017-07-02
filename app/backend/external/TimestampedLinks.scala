package backend.external

import backend.recon.Reconcilable
import org.joda.time.DateTime

case class TimestampedLinks[R <: Reconcilable](links: BaseLinks[R], timestamp: DateTime)
case class TimestampedExtendedLinks[R <: Reconcilable](links: ExtendedLinks[R], timestamp: DateTime)
