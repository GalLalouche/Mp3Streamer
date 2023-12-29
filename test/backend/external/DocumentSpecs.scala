package backend.external

import io.lemonlabs.uri.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Suite

import common.rich.path.RichFile._
import common.test.AuxSpecs

trait DocumentSpecs extends AuxSpecs { self: Suite =>
  def getDocument(path: String): Document = Jsoup.parse(getResourceFile(path), "UTF-8")
  def getBytes(path: String): Array[Byte] = getResourceFile(path).bytes
  def getBytes(url: Url): Array[Byte] = getBytes(url.toStringPunycode)
}
