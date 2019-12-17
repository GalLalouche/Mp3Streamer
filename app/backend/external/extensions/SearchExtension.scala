package backend.external.extensions

import backend.Url
import backend.external.{ExtendedLinks, Host, LinkMark}
import backend.recon.Reconcilable

import common.rich.RichT._

private[external] object SearchExtension {
  def apply[R <: Reconcilable](h: Host, r: R): ExtendedLink[R] =
    ExtendedLink(Url("javascript:void(0)"), h, mark = LinkMark.Missing,
      Vector(LinkExtension("Google", s"http://www.google.com/search?q=${r.normalize} ${h.name}" |> Url)))

  def extendMissing[R <: Reconcilable](allHosts: TraversableOnce[Host], r: R)(links: ExtendedLinks[R]): ExtendedLinks[R] =
    links ++ (allHosts.toSet &~ links.map(_.host).toSet map (apply(_, r)))
}
