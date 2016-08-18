package backend.external

import common.AuxSpecs
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Suite

trait DocumentSpecs extends AuxSpecs {
  self: Suite =>

  def getDocument(name: String): Document = Jsoup.parse(getResourceFile(name), "UTF-8")
}
