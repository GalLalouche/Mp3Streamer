package backend.external.expansions

import backend.{FutureOption, Url}
import backend.external.{BaseLink, DocumentSpecs}
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import com.google.inject.{Guice, Module}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

abstract class SameHostExpanderSpec extends AsyncFreeSpec with DocumentSpecs {
  protected def module: Module

  protected def artistUrl = "Url"
  protected def expandingUrl = artistUrl
  protected def findAlbum(
      documentName: String,
      album: Album,
      additionalMappings: (String, String)*,
  ): FutureOption[BaseLink[Album]] = {
    val urlToBytesMapper: PartialFunction[Url, Array[Byte]] = {
      case Url(address) if address == expandingUrl => getBytes(documentName)
    }

    val injector = Guice.createInjector(
      module,
      TestModuleConfiguration(_urlToBytesMapper = urlToBytesMapper.orElse {
        case Url(address) => getBytes(additionalMappings.toMap.apply(address))
      }).module)
    val $ = injector.instance[SameHostExpander]
    $.apply(BaseLink[Artist](Url(artistUrl), $.host), album)
  }
}
