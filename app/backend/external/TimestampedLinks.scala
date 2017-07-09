package backend.external

import backend.recon.Reconcilable
import monocle.macros.Lenses
import org.joda.time.DateTime

case class TimestampedLinks[R <: Reconcilable](links: MarkedLinks[R], timestamp: DateTime)
@Lenses
case class TimestampedExtendedLinks[R <: Reconcilable](links: ExtendedLinks[R], timestamp: DateTime)
