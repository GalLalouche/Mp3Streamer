package backend.external

import monocle.macros.Lenses

import java.time.LocalDateTime

import backend.recon.Reconcilable

private case class TimestampedLinks[R <: Reconcilable](
    links: MarkedLinks[R],
    timestamp: LocalDateTime,
)
@Lenses
case class TimestampedExtendedLinks[R <: Reconcilable](
    links: ExtendedLinks[R],
    timestamp: LocalDateTime,
)
