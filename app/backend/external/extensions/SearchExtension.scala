package backend.external.extensions

import java.net.URLEncoder

import backend.Url
import backend.external.Host
import backend.recon.Reconcilable
import common.rich.RichT._

object SearchExtension {
  def apply[R <: Reconcilable](h: Host, r: R): ExtendedLink[R] =
    ExtendedLink(Url("javascript:void(0)"), h.copy(name = h.name + "?"), List(LinkExtension("Google",
      URLEncoder.encode(s"http://www.google.co.il/search?q=${r.normalize} ${h.name}", "UTF-8") |> Url)))
}
