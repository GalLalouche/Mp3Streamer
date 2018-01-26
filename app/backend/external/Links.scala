package backend.external

import backend.Url
import backend.recon.Reconcilable
import monocle.Iso
import monocle.macros.Lenses

@Lenses
case class BaseLink[R <: Reconcilable](link: Url, host: Host)
object BaseLink {
  def iso[R1 <: Reconcilable, R2 <: Reconcilable]: Iso[BaseLink[R1], BaseLink[R2]] =
    Iso.apply((e: BaseLink[R1]) => e.copy[R2]())((e: BaseLink[R2]) => e.copy[R1]())
}
private[external] case class MarkedLink[R <: Reconcilable](link: Url, host: Host, isNew: Boolean) {
  def toBase: BaseLink[R] = BaseLink(link, host)
}
private object MarkedLink {
  def markNew[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] =
    MarkedLink(link = bl.link, host = bl.host, isNew = true)
  def markExisting[R <: Reconcilable](bl: BaseLink[R]): MarkedLink[R] =
    MarkedLink(link = bl.link, host = bl.host, isNew = false)
}
