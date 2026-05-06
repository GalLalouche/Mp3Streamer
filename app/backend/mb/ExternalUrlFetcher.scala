package backend.mb

import backend.external.{BaseLink, BaseLinks, Host}
import backend.mb.ExternalUrlFetcher.parse
import backend.recon.{Reconcilable, ReconID}
import com.google.inject.Inject
import io.lemonlabs.uri.Url
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import common.json.RichJson.DynamicJson

private[backend] class ExternalUrlFetcher @Inject() (
    jsonDownloader: JsonDownloader,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply[R <: Reconcilable](metadataType: String)(id: ReconID): Future[BaseLinks[R]] = {
    val mbUrl = Url.parse(s"https://musicbrainz.org/$metadataType/${id.id}")
    val mbLink = BaseLink[R](mbUrl, Host.MusicBrainz)
    jsonDownloader(metadataType + "/" + id.id, "inc" -> "url-rels").map(mbLink +: parse[R](_))
  }
}

private object ExternalUrlFetcher {
  private def parse[R <: Reconcilable](json: JsObject): Seq[BaseLink[R]] =
    json
      .objects("relations")
      .flatMap { e =>
        val url = Url.parse((e / "url").str("resource"))
        Host.withUrl(url).map(BaseLink(url, _))
      }
}
