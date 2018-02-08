package backend.external.expansions

import backend.Url
import backend.configs.TestConfiguration
import backend.external.{BaseLink, DocumentSpecs}
import backend.recon.{Album, Artist}
import common.rich.RichFuture._
import org.scalatest.FreeSpec

trait SameHostExpanderSpec extends FreeSpec with DocumentSpecs {
  protected val url = "url"
  protected val expandingUrl = url
  private def configWithUrl(document: String) = TestConfiguration(_urlToBytesMapper = {
    case Url(address) if address == expandingUrl => getBytes(document)
  })
  private[expansions] def createExpander(implicit c: TestConfiguration): SameHostExpander
  protected def findAlbum(documentUrl: String, album: Album): Option[BaseLink[Album]] = {
    implicit val c = configWithUrl(documentUrl)
    val $ = createExpander
    $(BaseLink[Artist](Url(url), $.host), album).get
  }
}
