package backend.external.expansions

import backend.Url
import backend.configs.{Configuration, TestConfiguration}
import backend.external.{BaseLink, DocumentSpecs}
import backend.recon.{Album, Artist}
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

trait SameHostExpanderSpec extends FreeSpec with DocumentSpecs {
  protected val artistUrl = "Url"
  protected val expandingUrl = artistUrl
  private[expansions] def createExpander(implicit c: Configuration): SameHostExpander
  protected def findAlbum(documentName: String, album: Album,
      additionalMappings: (String, String)*): Option[BaseLink[Album]] = {
    val urlToBytesMapper: PartialFunction[Url, Array[Byte]] = {
      case Url(address) if address == expandingUrl => getBytes(documentName)
    }

    implicit val c: Configuration = TestConfiguration(_urlToBytesMapper =
        urlToBytesMapper.orElse {
          case Url(address) => getBytes(additionalMappings.toMap.apply(address))
        })
    implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
    val $ = createExpander
    $(BaseLink[Artist](Url(artistUrl), $.host), album).get
  }
}
