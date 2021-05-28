package mains.vimtag

import common.rich.RichT._

private trait IndividualParser {
  def splitMap(lines: Seq[String]): Map[String, String] =
    lines.flatMap(_.trim.split(": ?", 2).opt.filter(_.length == 2).map(_.toTuple(_ (0), _ (1)))).toMap
  def cutoff: String => Boolean
  def apply(xs: Seq[String]): Seq[IndividualId3]
}
