package backend.external

import backend.Url
import common.AuxSpecs
import common.rich.path.RichFile._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Suite

trait DocumentSpecs extends AuxSpecs { self: Suite =>

  def getDocument(name: String): Document = Jsoup.parse(getResourceFile(name), "UTF-8")
  def getBytes(name: String): Array[Byte] = getResourceFile(name).bytes
  def getBytes(url: Url): Array[Byte] = getBytes(url.address)
}