package mains.vimtag.table

import common.rich.RichT._
import mains.vimtag.IndividualInitializer
import mains.vimtag.IndividualInitializer.IndividualTags

private object TableInitializer extends IndividualInitializer {
  override def apply(tags: Seq[IndividualInitializer.IndividualTags]): Seq[String] = {
    def reorder(t: IndividualTags): Seq[Any] = Vector(t.track, t.title, t.discNumber, t.file)
    def toTableLine(ss: TraversableOnce[Any]) = ss.mkString("| ", " | ", " |")
    val headlines = Vector(
      "Track #",
      "Title",
      "Disc Number",
      "File",
    ) |> toTableLine
    val sep = "|-+"

    (sep :: headlines :: sep :: tags.map(reorder _ andThen toTableLine).toList) :+ sep
  }
}
