package models

import scala.collection.JavaConverters._

import common.rich.RichT._
import org.jaudiotagger.tag.{FieldKey, Tag}

object RichTag {
  private val customPattern = """Description="([^"]+)"; Text="([^"]+)"; """.r
  implicit class richTag(private val $ : Tag) extends AnyVal {
    private def filterNonEmpty(s: String): Option[String] = s.trim.optFilter(_.nonEmpty)
    def setOption(key: FieldKey, o: Option[Any]): Unit =
      o.map(_.toString).foreach($.setField(key, _))
    def firstNonEmpty(key: String): Option[String] = $.getFirst(key) |> filterNonEmpty
    def firstNonEmpty(key: FieldKey): Option[String] = $.getFirst(key) |> filterNonEmpty
    def customTags: Map[String, String] = $.getFields("TXXX").asScala
      .map(_.toString)
      .collect { case customPattern(key, value) => key -> value }
      .toMap
  }
}
