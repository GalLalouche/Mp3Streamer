package backend.external.extensions

import backend.external.{ExtendedLink, ExternalLink, Host}
import backend.recon.{Album, Artist, Reconcilable}

class CompositeExtender(artistExtensions: Map[Host, LinkExtender[Artist]], albumExtensions: Map[Host, LinkExtender[Album]]) {
  private def auxExtend[R <: Reconcilable](e: ExternalLink[R], map: Map[Host, LinkExtender[R]]): ExtendedLink[R] =
    ExtendedLink.extend(e).withLinks(map.get(e.host).map(_ (e)).getOrElse(Nil))

  private val artistClass = classOf[Artist]
  private val albumClass = classOf[Album]
  def apply[R <: Reconcilable : Manifest](e: ExternalLink[R]): ExtendedLink[R] = {
    val map = implicitly[Manifest[R]].runtimeClass match {
      case `artistClass` => artistExtensions
      case `albumClass` => albumExtensions
    }
    auxExtend(e, map.asInstanceOf[Map[Host, LinkExtender[R]]])
  }
}
