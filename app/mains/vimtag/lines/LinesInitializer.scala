package mains.vimtag.lines

import mains.vimtag.IndividualInitializer
import mains.vimtag.IndividualInitializer.IndividualTags

private object LinesInitializer extends IndividualInitializer {
  override def apply(tags: Seq[IndividualInitializer.IndividualTags]) = tags.flatMap {
    case IndividualTags(file, title, track, discNumber) =>
      Vector(
        s"FILE: $file",
        s"TITLE: $title",
        s"TRACK: $track",
        s"DISC_NO: $discNumber",
      )
  }
}
