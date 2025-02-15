package common

import org.jaudiotagger.tag.{FieldKey, Tag}

import common.rich.RichT._

object TagUtils {
  implicit class richTag(private val $ : Tag) extends AnyVal {
    private def filterNonEmpty(s: String): Option[String] = s.trim.optFilter(_.nonEmpty)
    def setOption(key: FieldKey, o: Option[Any]): Unit =
      o.map(_.toString).foreach($.setField(key, _))
    def firstNonEmpty(key: String): Option[String] = $.getFirst(key) |> filterNonEmpty
    def firstNonEmpty(key: FieldKey): Option[String] = $.getFirst(key) |> filterNonEmpty
  }
}
