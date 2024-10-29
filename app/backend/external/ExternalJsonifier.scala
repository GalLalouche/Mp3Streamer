package backend.external

import java.time.format.DateTimeFormatter
import javax.inject.Inject

import backend.external.ExternalJsonifier._
import backend.external.Host.{Wikidata, Wikipedia}
import backend.external.extensions.{ExtendedLink, LinkExtension}
import play.api.libs.json.{JsObject, Json, JsString}
import play.api.libs.json.Json.JsValueWrapper

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._

private class ExternalJsonifier @Inject() (implicit ec: ExecutionContext) {
  private type KVPair = (String, JsValueWrapper)
  private def formatHost(e: ExtendedLink[_]): String = e.host.name + (e.mark match {
    case LinkMark.None => ""
    case LinkMark.New => "*"
    case LinkMark.Missing => "?"
    case LinkMark.Text(s) => s"* $s" // Currently, Text is only added to new links
  })
  private def toJson(extension: LinkExtension[_]): KVPair =
    extension.name -> extension.link.toStringPunycode
  private def toJson(link: ExtendedLink[_]): KVPair = link.host.name -> Json.obj(
    // This has to be canonized late, otherwise the "*" will be deleted.
    "host" -> formatHost(link),
    "main" -> link.link.toStringPunycode,
    "extensions" -> Json.obj(link.extensions.map(toJson).toVector: _*),
  )
  private def toJson(linkses: Traversable[ExtendedLink[_]]): JsObject =
    linkses
      .filterAndSortBy(_.host, Hosts)
      // Filter non-new Wikidata, because they aren't shown in the client.
      .filter(e => e.host != Wikidata || e.isNew || e.hasText)
      // Unmark new Wikipedia links, because MusicBrainz only uses Wikidata now.
      // Using mapIf messes up the link's type inference due to existential types.
      .map(e => if (e.host == Wikipedia && e.isNew) e.unmark else e)
      .map(toJson) |> Json.obj
  private def toJson(e: TimestampedExtendedLinks[_]): JsObject = Json.obj(
    "links" -> toJson(e.links),
    "timestamp" -> JsString(e.timestamp |> DateStringPattern.format),
  )

  def toJsonOrError(links: Future[TimestampedExtendedLinks[_]]): Future[JsObject] =
    links.map(toJson).handleErrorFlat { e =>
      scribe.error(e)
      Json.obj("error" -> e.getMessage)
    }
}

object ExternalJsonifier {
  private val DateStringPattern = DateTimeFormatter.ofPattern("dd/MM")
  val Hosts: Seq[Host] = Vector(
    Host.MusicBrainz,
    Host.Wikipedia,
    Host.Wikidata,
    Host.AllMusic,
    Host.Facebook,
    Host.LastFm,
    Host.RateYourMusic,
  )
}
