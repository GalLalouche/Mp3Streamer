package backend.external

import java.time.format.DateTimeFormatter

import backend.external.extensions.{ExtendedLink, LinkExtension}
import backend.external.ExternalJsonifier._
import backend.external.Host.{Wikidata, Wikipedia}
import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.rich.func.ToMoreMonadErrorOps
import javax.inject.Inject
import play.api.libs.json.{JsObject, Json, JsString}
import play.api.libs.json.Json.JsValueWrapper

import scala.concurrent.{ExecutionContext, Future}

import scalaz.std.FutureInstances

private class ExternalJsonifier @Inject()(implicit ec: ExecutionContext)
    extends ToMoreMonadErrorOps with FutureInstances {
  private type KVPair = (String, JsValueWrapper)
  private def canonizeHost(e: ExtendedLink[_]): String = {
    val isMissing = e.host.name.endsWith("?")
    val canonized = e.host.canonize.name

    if (e.isNew) canonized + "*"
    else if (isMissing) canonized + "?"
    else canonized
  }
  private def toJson(extension: LinkExtension[_]): KVPair = extension.name -> extension.link.address
  private def toJson(link: ExtendedLink[_]): KVPair = link.host.name -> Json.obj(
    // This has to be canonized late, otherwise the "*" will be deleted.
    "host" -> canonizeHost(link),
    "main" -> link.link.address,
    "extensions" -> Json.obj(link.extensions.map(toJson).toSeq: _*),
  )
  private def toJson(linkses: Traversable[ExtendedLink[_]]): JsObject =
    linkses.filterAndSortBy(_.host.canonize, Hosts)
        // Filter non-new Wikidata, because there's we don't show them in the client.
        .filter(e => e.host.canonize != Wikidata || e.isNew)
        // Unmark new Wikipedia links, because MusicBrainz only uses Wikidata now.
        // Using mapIf messes up the link's type inference due to existential types.
        .map(e => if (e.host.canonize == Wikipedia) e.unmark else e)
        .map(toJson) |> Json.obj
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject =
    toJson(e.links) + ("timestamp" -> JsString(e.timestamp |> DateStringPattern.format))

  def toJsonOrError(links: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    links.map(toJson).handleErrorFlat(e => Json.obj("error" -> e.getMessage))
}

object ExternalJsonifier {
  private val DateStringPattern = DateTimeFormatter.ofPattern("dd/MM")
  val Hosts: Seq[Host] = Vector(
    Host.MusicBrainz, Host.Wikipedia, Host.Wikidata, Host.AllMusic, Host.Facebook, Host.LastFm, Host.RateYourMusic)
}