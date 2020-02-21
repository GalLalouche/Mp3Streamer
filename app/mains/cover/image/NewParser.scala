package mains.cover.image

import backend.Url
import mains.cover.UrlSource
import org.jsoup.nodes.Document
import play.api.libs.json.{JsArray, Json}

import common.json.RichJson._
import common.rich.collections.RichIterator._
import common.rich.primitives.RichString._
import common.RichJsoup._

/**
* An even more annoying parser for Google's most recent (at the time of writing this code) image search
* results.
*/
private object NewParser extends HtmlParser {
  override def apply(d: Document): Option[Seq[UrlSource]] = (for {
    // A magical class which contains a link that is later referenced in the <script> elements.
    anchor <- d.selectIterator("a.VFACy.kGQAp")
    href = anchor.href
    // The *actual* links are hidden inside <script> elements.
    script <- d.selectIterator("script")
    innerHtml = script.html
    // Find the JavaScript <script> element that contains the links.
    if innerHtml contains href
  } yield {
    // This script should have a single return expression which returns an array...
    val json = Json.parse(innerHtml.dropWhile(_ != '[').dropAfterLast(']')).as[JsArray]
    def dig(js: JsArray): JsArray = js.value.find(_.toString contains href).get.as[JsArray]
    // The actual links are nested pretty deeply (level 4), so we find the correct sub-sub-sub-sub element
    // containing the links...
    val internalArray = Iterator.iterate(json)(dig)(4)
    internalArray.value.view
        .filter(_ has 1)
        // Again, the actual links are nested within each element, but this time we can't search for the
        // head links, since the array we're actually interested in does not contain said links.
        .map(_ arrayAt 1 arrayAt 3 ensuring (_.value.length == 3))
        .map(a => UrlSource(Url(a stringAt 0), a intAt 1, a intAt 2))
        .toVector
  }).headOption()
}
