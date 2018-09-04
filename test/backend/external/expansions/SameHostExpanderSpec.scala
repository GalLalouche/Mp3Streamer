package backend.external.expansions

import backend.Url
import backend.external.{BaseLink, DocumentSpecs}
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import com.google.inject.{Guice, Module}
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

abstract class SameHostExpanderSpec extends FreeSpec with DocumentSpecs {
  protected def module: Module

  protected def artistUrl = "Url"
  protected def expandingUrl = artistUrl
  protected def findAlbum(documentName: String, album: Album,
      additionalMappings: (String, String)*): Option[BaseLink[Album]] = {
    val urlToBytesMapper: PartialFunction[Url, Array[Byte]] = {
      case Url(address) if address == expandingUrl => getBytes(documentName)
    }

    val injector = Guice.createInjector(
      module,
      TestModuleConfiguration(_urlToBytesMapper = urlToBytesMapper.orElse {
        case Url(address) => getBytes(additionalMappings.toMap.apply(address))
      }).module)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[SameHostExpander]
    $.apply(BaseLink[Artist](Url(artistUrl), $.host), album).get
  }
}
