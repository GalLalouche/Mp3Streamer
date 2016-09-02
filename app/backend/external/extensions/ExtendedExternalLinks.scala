package backend.external.extensions

import backend.external._
import backend.recon._
import org.joda.time.DateTime

case class ExtendedExternalLinks(artistLinks: ExtendedLinks[Artist],
                                 artistTimeStamp: DateTime,
                                 albumLinks: ExtendedLinks[Album],
                                 albumTimestamp: DateTime,
                                 trackLinks: ExtendedLinks[Track],
                                 trackTimestamp: DateTime)
