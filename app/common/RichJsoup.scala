package common

import org.jsoup.nodes.Element

import scala.collection.JavaConverters._

import common.rich.collections.RichTraversableOnce._

object RichJsoup {
  implicit class richElement(private val $ : Element) extends AnyVal {
    def selectIterator(cssQuery: String): Iterator[Element] = $.select(cssQuery).iterator.asScala
    def selectSingle(cssQuery: String): Element = selectIterator(cssQuery).single
    def selectSingleOpt(cssQuery: String): Option[Element] = selectIterator(cssQuery).singleOpt
    def find(cssQuery: String): Option[Element] = Option($.selectFirst(cssQuery))

    def href = $.attr("href")
  }
}
