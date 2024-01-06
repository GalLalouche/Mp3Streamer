package mains.vimtag

import mains.vimtag.IndividualInitializer.IndividualTags
import models.Song.TrackNumber

private trait IndividualInitializer {
  def apply(tags: Seq[IndividualTags]): Seq[String]
}
private object IndividualInitializer {
  case class IndividualTags(file: String, title: String, track: TrackNumber, discNumber: String)
}
