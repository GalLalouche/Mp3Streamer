package backend.external.extensions

import backend.Url
import backend.external.{Host, MarkedLink}
import backend.recon.Reconcilable

private[external] case class LinkExtension[R <: Reconcilable](name: String, link: Url)
private[external] case class ExtendedLink[R <: Reconcilable](link: Url, host: Host, isNew: Boolean,
                                           extensions: Traversable[LinkExtension[R]]) {
  def unmark: ExtendedLink[_] = copy(isNew = false)
}
private object ExtendedLink {
  // TODO generify
  def extend[R <: Reconcilable](e: MarkedLink[R]) = new {
    def withLinks(links: Traversable[LinkExtension[R]]): ExtendedLink[R] = ExtendedLink[R](e.link, e.host, e.isNew, links)
  }
}
