package backend.external.expansions

import backend.external.{BaseLinks, Host}
import backend.external.recons.LinkRetrievers
import backend.recon.{Album, Artist}
import javax.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.collections.RichTraversableOnce._

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums. */
private[external] class CompositeSameHostExpander @Inject() (
    wiki: WikipediaAlbumFinder,
    am: AllMusicAlbumFinder,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  private val expanders: Map[Host, SameHostExpander] = Iterator(wiki, am).mapBy(_.host)
  def toReconcilers(ls: BaseLinks[Artist]): LinkRetrievers[Album] = {
    val availableHosts = ls.toMultiMap(_.host).mapValues(_.head)
    LinkRetrievers(expanders.flatMap(e => availableHosts.get(e._1).map(e._2.toReconciler)))
  }
}
