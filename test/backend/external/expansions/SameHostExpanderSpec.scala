package backend.external.expansions

import backend.FutureOption
import backend.external.{BaseLink, DocumentSpecs}
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import com.google.inject.{Guice, Module}
import common.test.AsyncAuxSpecs
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

abstract class SameHostExpanderSpec extends AsyncFreeSpec with DocumentSpecs with AsyncAuxSpecs {
  protected def module: Module

  protected def artistUrl = "Url"
  protected def expandingUrl = artistUrl
  protected def findAlbum(
      documentName: String,
      album: Album,
      additionalMappings: (String, String)*,
  ): FutureOption[BaseLink[Album]] = {
    val urlToBytesMapper: PartialFunction[Url, Array[Byte]] = {
      case address if address.toStringPunycode == expandingUrl => getBytes(documentName)
    }

    val map = additionalMappings.toMap
    val injector = Guice.createInjector(
      module,
      TestModuleConfiguration(_urlToBytesMapper = urlToBytesMapper.orElse {
        case address if map.contains(address.toStringPunycode) =>
          getBytes(map.apply(address.toStringPunycode))
      }).module,
    )
    val $ = injector.instance[SameHostExpander]
    $.apply(BaseLink[Artist](Url.parse(artistUrl), $.host), album)
  }
}
