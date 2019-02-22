package models

import common.rich.RichT._
import common.rich.primitives.RichString._
import org.jaudiotagger.tag.{FieldKey, Tag}

import scala.collection.JavaConverters._

object RichTag {
  private val customPattern = """Description="([^"]+)"; Text="([^"]+)"; """.r
  implicit class richTag(private val $: Tag) extends AnyVal {
    private def filterNonEmpty(s: String): Option[String] = s.trim.opt.filter(_.nonEmpty)
    def setOption(key: FieldKey, o: Option[Any]): Tag = {
      o.foreach(f => $.setField(key, f.toString))
      $
    }
    def firstNonEmpty(key: String): Option[String] = $.getFirst(key) |> filterNonEmpty
    def firstNonEmpty(key: FieldKey): Option[String] = $.getFirst(key) |> filterNonEmpty
    def customTags: Map[String, String] = $.getFields("TXXX").asScala
        .map(_.toString)
        .filter(_ matches customPattern.pattern)
        .map({case customPattern(key, value) => key -> value})
        .toMap
  }
}
