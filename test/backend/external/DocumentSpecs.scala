package backend.external

import org.scalatest.Suite

import backend.Url
import common.rich.path.RichFile._
import common.test.AuxSpecs
import org.jsoup.nodes.Document
import org.jsoup.Jsoup

trait DocumentSpecs extends AuxSpecs { self: Suite =>
  def getDocument(path: String): Document = Jsoup.parse(getResourceFile(path), "UTF-8")
  def getBytes(path: String): Array[Byte] = getResourceFile(path).bytes
  def getBytes(url: Url): Array[Byte] = getBytes(url.address)
}
