package backend.external.extensions

import backend.Url
import backend.external.{ExtendedLinks, Host}
import backend.recon.Reconcilable
import common.rich.RichT._
import common.rich.collections.RichSet._

private[external] object SearchExtension {
  def apply[R <: Reconcilable](h: Host, r: R): ExtendedLink[R] =
    ExtendedLink(Url("javascript:void(0)"), Host.name.modify(_ + "?")(h), isNew = false,
      List(LinkExtension("Google",
        s"http://www.google.com/search?q=${r.normalize} ${h.name}" |> Url)))

  def extendMissing[R <: Reconcilable](allHosts: TraversableOnce[Host], r: R)(links: ExtendedLinks[R]): ExtendedLinks[R] =
    links ++ (allHosts.toSet &~ links.map(_.host.canonize).toSet map (apply(_, r)))
}
