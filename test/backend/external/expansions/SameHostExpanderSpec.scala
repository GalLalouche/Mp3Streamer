package backend.external.expansions

import backend.Url
import backend.configs.TestModuleConfiguration
import backend.external.{BaseLink, DocumentSpecs}
import backend.recon.{Album, Artist}
import com.google.inject.{Binder, Guice, Module}
import common.rich.RichFuture._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.ExecutionContext

trait SameHostExpanderSpec extends FreeSpec with DocumentSpecs with Module {
  override def configure(binder: Binder) = {
    // A poor man's require binding
    binder.getProvider(classOf[SameHostExpander])
  }
  protected val artistUrl = "Url"
  protected val expandingUrl = artistUrl
  protected def findAlbum(documentName: String, album: Album,
      additionalMappings: (String, String)*): Option[BaseLink[Album]] = {
    val urlToBytesMapper: PartialFunction[Url, Array[Byte]] = {
      case Url(address) if address == expandingUrl => getBytes(documentName)
    }

    val injector = Guice.createInjector(
      this,
      TestModuleConfiguration(_urlToBytesMapper = urlToBytesMapper.orElse {
        case Url(address) => getBytes(additionalMappings.toMap.apply(address))
      }).module)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val $ = injector.instance[SameHostExpander]
    $(BaseLink[Artist](Url(artistUrl), $.host), album).get
  }
}
