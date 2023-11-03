package mains.vimtag

import common.rich.collections.RichIterable._
import common.rich.collections.RichTraversableOnce._

private sealed trait GlobalField {
  def key: String
  def values: Seq[Any]
  protected def lineForEmpty: String
  private def fromValue(s: Any) = s"$key: $s"
  def lines: Seq[String] = values.toList.distinct match {
    case Nil => Vector(fromValue(lineForEmpty))
    case single :: Nil => Vector(fromValue(single))
    case _ =>
      assert(values.hasAtLeastSizeOf(2))
      val counts = values.frequencies.toVector.sortBy(-_._2)
      val mostCommonIsMoreThanHalf = counts.head._2 > values.size / 2
      Vector(
        "# " + counts.map { case (name, count) => s"$name ($count)" }.mkString("/"),
        fromValue(if (mostCommonIsMoreThanHalf) Tags.Common else Tags.Keep),
      )
  }
}
private case class RequiredField(override val key: String, override val values: Seq[Any])
    extends GlobalField {
  protected override def lineForEmpty = Tags.ExplicitEmpty
}
private case class OptionalField(override val key: String, override val values: Seq[Any])
    extends GlobalField {
  protected override def lineForEmpty = ""
}
