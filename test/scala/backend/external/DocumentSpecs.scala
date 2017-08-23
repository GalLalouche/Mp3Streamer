package backend.external

import java.io.{FileInputStream, InputStream}

import backend.Url
import common.AuxSpecs
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Suite

trait DocumentSpecs extends AuxSpecs {
  self: Suite =>

  def getDocument(name: String): Document = Jsoup.parse(getResourceFile(name), "UTF-8")
  def getStream(name: String): InputStream = new FileInputStream(getResourceFile(name))
  def getStream(url: Url): InputStream = getStream(url.address)
}
