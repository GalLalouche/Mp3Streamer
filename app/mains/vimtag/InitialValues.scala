package mains.vimtag

import common.rich.collections.RichTraversableOnce._

private class InitialValues(allTagValues: Seq[String]) {
  def mostCommon: String = allTagValues.frequencies.toSeq.minBy(-_._2)._1
}

private object InitialValues {
  def from(xs: GlobalField*): Map[String, InitialValues] =
    xs.map(e => e.key -> new InitialValues(e.values.map(_.toString))).toMap
}
