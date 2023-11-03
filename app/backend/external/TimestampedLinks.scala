package backend.external

import java.time.LocalDateTime

import backend.recon.Reconcilable

import monocle.macros.Lenses

private case class TimestampedLinks[R <: Reconcilable](
    links: MarkedLinks[R],
    timestamp: LocalDateTime,
)
@Lenses
case class TimestampedExtendedLinks[R <: Reconcilable](
    links: ExtendedLinks[R],
    timestamp: LocalDateTime,
)
