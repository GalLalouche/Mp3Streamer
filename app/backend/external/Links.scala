package backend.external
import backend.Url
import backend.recon.Reconcilable

private[external] case class BaseLink[R <: Reconcilable](link: Url, host: Host)
private[external] case class MarkedLink[R <: Reconcilable](link: Url, host: Host, isNew: Boolean) {
  def toBase: BaseLink[R] = BaseLink(link, host)
}
private object MarkedLink {
  def markNew[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] =
    MarkedLink(link = bl.link, host = bl.host, isNew = true)
  def markExisting[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] =
    MarkedLink(link = bl.link, host = bl.host, isNew = false)
}
