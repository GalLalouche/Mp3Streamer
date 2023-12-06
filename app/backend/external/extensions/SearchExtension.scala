package backend.external.extensions

import backend.external.{ExtendedLinks, Host, LinkMark}
import backend.recon.Reconcilable
import backend.Url
import common.rich.RichT.richT
import common.Urls

private[external] object SearchExtension {
  def apply[R <: Reconcilable](h: Host, r: R): ExtendedLink[R] = {
    val query = s"${r.normalize} ${h.name}"
    ExtendedLink(
      Url("javascript:void(0)"),
      h,
      mark = LinkMark.Missing,
      Vector(
        LinkExtension("Google", Urls.googleSearch(query).|>(backend.Url.from)),
        LinkExtension("Lucky", backend.Url("lucky/redirect/" + query)),
      ),
    )
  }

  def extendMissing[R <: Reconcilable](allHosts: TraversableOnce[Host], r: R)(
      links: ExtendedLinks[R],
  ): ExtendedLinks[R] =
    links ++ (allHosts.toSet &~ links.map(_.host).toSet map (apply(_, r)))
}
