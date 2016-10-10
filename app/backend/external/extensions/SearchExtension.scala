package backend.external.extensions

import java.net.URLEncoder

import backend.Url
import backend.external.{ExtendedLinks, Host}
import backend.recon.Reconcilable
import common.rich.RichT._
import common.rich.collections.RichSet._

object SearchExtension {
  def apply[R <: Reconcilable](h: Host, r: R): ExtendedLink[R] =
    ExtendedLink(Url("javascript:void(0)"), h.copy(name = h.name + "?"), List(LinkExtension("Google",
      s"http://www.google.co.il/search?q=${URLEncoder.encode(s"${r.normalize} ${h.name}", "UTF-8")}" |> Url)))

  def extendMissing[R <: Reconcilable](allHosts: TraversableOnce[Host], r: R, links: ExtendedLinks[R]): ExtendedLinks[R] =
    links ++ (allHosts.toSet \ links.map(_.host.canonize).toSet map (SearchExtension(_, r)))
}
