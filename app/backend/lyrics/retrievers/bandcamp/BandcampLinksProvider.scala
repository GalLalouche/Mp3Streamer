package backend.lyrics.retrievers.bandcamp

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT

import backend.{FutureOption, Url}
import backend.external.{Host, MbHtmlLinkExtractorHelper}
import backend.mb.ReleaseGroupToReleases
import backend.recon.{Album, ReconID}
import backend.recon.Reconcilable.SongExtractor
import common.rich.collections.RichTraversableOnce._
import common.rich.func.BetterFutureInstances._
import models.Song

/**
 * MusicBrainz is supposed to allow adding Bandcamp lyric links for release groups, which would have
 * been great since then one could simply fetch the bandcamp link from the release group external
 * links, which are fetched anyway. But they don't. However, they *do* allow Bandcamp links in
 * specific releases. So this class fetches the external links of the releases, and hopefully
 * something useful will come from that.
 */
private class BandcampLinksProvider @Inject() (
    musicBrainzReleaseFetcher: ReleaseGroupToReleases,
    mbHtmlLinkExtractorHelper: MbHtmlLinkExtractorHelper,
    ec: ExecutionContext,
) {
  implicit val iec: ExecutionContext = ec
  private def getBandcampLink(releaseID: ReconID): Future[Option[Url]] =
    mbHtmlLinkExtractorHelper[Album]("release")(releaseID)
      .map(_.find(_.host == Host.Bandcamp).map(_.link))
  def apply(s: Song): FutureOption[Url] = OptionT(
    musicBrainzReleaseFetcher(s.release)
      .flatMap(_.mapFirstF(getBandcampLink)),
  )
}
