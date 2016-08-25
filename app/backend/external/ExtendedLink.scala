package backend.external

import backend.Url
import backend.recon.Reconcilable

case class LinkExtensions[R <: Reconcilable](name: String, link: Url)
case class ExtendedLink[R <: Reconcilable](link: Url, host: Host, extensions: Traversable[LinkExtensions[R]]) {
  def toLink = ExternalLink[R](link, host)
}


object ExtendedLink {
  def extend[R <: Reconcilable](e: ExternalLink[R]) = new {
    def withLinks(links: Traversable[LinkExtensions[R]]) = new ExtendedLink[R](e.link, e.host, links)
  }
}
