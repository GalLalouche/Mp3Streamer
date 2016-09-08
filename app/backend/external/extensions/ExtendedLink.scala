package backend.external.extensions

import backend.Url
import backend.external.{ExternalLink, Host}
import backend.recon.Reconcilable

case class LinkExtension[R <: Reconcilable](name: String, link: Url)
case class ExtendedLink[R <: Reconcilable](link: Url, host: Host, extensions: Traversable[LinkExtension[R]])


private object ExtendedLink {
  def extend[R <: Reconcilable](e: ExternalLink[R]) = new {
    def withLinks(links: Traversable[LinkExtension[R]]) = new ExtendedLink[R](e.link, e.host, links)
  }
}
