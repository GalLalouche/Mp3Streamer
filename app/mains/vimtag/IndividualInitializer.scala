package mains.vimtag

import mains.vimtag.IndividualInitializer.IndividualTags
import models.{SongTitle, TrackNumber}

private trait IndividualInitializer {
  def apply(tags: Seq[IndividualTags]): Seq[String]
}
private object IndividualInitializer {
  case class IndividualTags(file: String, title: SongTitle, track: TrackNumber, discNumber: String)
}
