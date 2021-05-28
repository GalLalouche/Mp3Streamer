package mains.vimtag

import mains.vimtag.IndividualInitializer.IndividualTags

private trait IndividualInitializer {
  def apply(tags: Seq[IndividualTags]): Seq[String]
}
private object IndividualInitializer {
  case class IndividualTags(file: String, title: String, track: Int, discNumber: String)
}
