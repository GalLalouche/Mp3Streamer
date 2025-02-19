package backend.external

import backend.recon.Reconcilable
import io.lemonlabs.uri.Url

import monocle.Iso
import monocle.macros.Lenses

@Lenses
case class BaseLink[R <: Reconcilable](link: Url, host: Host)
object BaseLink {
  def iso[R1 <: Reconcilable, R2 <: Reconcilable]: Iso[BaseLink[R1], BaseLink[R2]] =
    Iso[BaseLink[R1], BaseLink[R2]](_.copy())(_.copy())
}

private[external] case class MarkedLink[R <: Reconcilable](link: Url, host: Host, mark: LinkMark) {
  def isNew = mark == LinkMark.New
  def toBase: BaseLink[R] = BaseLink(link, host)
}
private object MarkedLink {
  private def mark[R <: Reconcilable](mark: LinkMark): BaseLink[R] => MarkedLink[R] =
    bl => MarkedLink[R](link = bl.link, host = bl.host, mark = mark)
  def markExisting[R <: Reconcilable]: BaseLink[R] => MarkedLink[R] = mark(LinkMark.None)
  def markNew[R <: Reconcilable]: BaseLink[R] => MarkedLink[R] = mark(LinkMark.New)
  def markMissing[R <: Reconcilable]: BaseLink[R] => MarkedLink[R] = mark(LinkMark.Missing)
}
