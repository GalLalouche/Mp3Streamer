package backend.external

import backend.Url
import backend.recon.Reconcilable
import monocle.Iso
import monocle.macros.Lenses

@Lenses
case class BaseLink[R <: Reconcilable](link: Url, host: Host)
object BaseLink {
  def iso[R1 <: Reconcilable, R2 <: Reconcilable]: Iso[BaseLink[R1], BaseLink[R2]] =
    Iso[BaseLink[R1], BaseLink[R2]](_.copy())(_.copy())
}
private[external] case class MarkedLink[R <: Reconcilable](link: Url, host: Host, isNew: Boolean) {
  def toBase: BaseLink[R] = BaseLink(link, host)
}
private object MarkedLink {
  private def mark[R <: Reconcilable](bl: BaseLink[R], isNew: Boolean) =
    MarkedLink[R](link = bl.link, host = bl.host, isNew = isNew)
  def markNew[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] = mark(bl, isNew = true)
  def markExisting[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] = mark(bl, isNew = false)
}
