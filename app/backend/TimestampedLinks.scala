package backend

import backend.external.Links
import backend.recon.Reconcilable
import org.joda.time.DateTime

case class TimestampedLinks[R <: Reconcilable](links: Links[R], timestamp: DateTime)
