package backend.external.extensions

import backend.Url
import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.Reconcilable

private[external] case class LinkExtension[R <: Reconcilable](name: String, link: Url)
private[external] case class ExtendedLink[R <: Reconcilable](
    link: Url, host: Host, mark: LinkMark, extensions: Traversable[LinkExtension[R]]) {
  def isNew = mark == LinkMark.New
  def hasText = mark.isInstanceOf[LinkMark.Text]

  def unmark: ExtendedLink[_] = copy(mark = LinkMark.None)
}
private object ExtendedLink {
  // TODO generify
  def extend[R <: Reconcilable](e: MarkedLink[R]) = new {
    def withLinks(links: Traversable[LinkExtension[R]]) = ExtendedLink[R](e.link, e.host, e.mark, links)
  }
}
