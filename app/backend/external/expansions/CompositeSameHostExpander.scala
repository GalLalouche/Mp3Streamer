package backend.external.expansions

import backend.external.{BaseLinks, Host}
import backend.external.recons.LinkRetrievers
import backend.recon.{Album, Artist}
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.collections.RichTraversableOnce._

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums. */
private[external] class CompositeSameHostExpander @Inject() (
    wiki: WikipediaAlbumFinder,
    // This is turned off since the discography loads dynamically and this is not something the
    // InternetTalker currently supports. See https://github.com/GalLalouche/Mp3Streamer/issues/145.
    // am: AllMusicAlbumFinder,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  private val expanders: Map[Host, SameHostExpander] = Iterator(wiki).mapBy(_.host)
  def toReconcilers(ls: BaseLinks[Artist]): LinkRetrievers[Album] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    LinkRetrievers(expanders.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler)))
  }
}
