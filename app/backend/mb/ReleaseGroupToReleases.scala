package backend.mb

import backend.recon.{Album, Reconciler, ReconID}
import com.google.inject.Inject
import play.api.libs.json.JsObject

import scala.concurrent.{ExecutionContext, Future}

import common.json.RichJson._

/**
 * A release group is an proper album, whereas a release is a specific version of said album. A
 * release entity can have different external links than the group entity, which is why we're
 * interested in releases to begin with.
 */
private[backend] class ReleaseGroupToReleases @Inject() (
    albumReconciler: Reconciler[Album],
    jsonDownloader: JsonDownloader,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(a: Album): Future[Iterable[ReconID]] = albumReconciler(a).value
    .map(_.get)
    .flatMap(reconId =>
      jsonDownloader(
        method = "release-group/" + reconId.id,
        params = "inc" -> "releases",
      ),
    )
    .map(ReleaseGroupToReleases.parse)
}

private object ReleaseGroupToReleases {
  private def parse(js: JsObject): Iterable[ReconID] =
    js.objects("releases").map(_.str("id")).map(ReconID.validateOrThrow)
}
