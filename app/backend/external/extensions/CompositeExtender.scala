package backend.external.extensions

import backend.external._
import backend.recon.{Album, Artist, Reconcilable}

private[external] class CompositeExtender private(artistExtensions: HostMap[LinkExtender[Artist]], albumExtensions: HostMap[LinkExtender[Album]]) {
  private def auxExtend[R <: Reconcilable](entity: R, e: MarkedLink[R], map: HostMap[LinkExtender[R]]): ExtendedLink[R] =
    ExtendedLink.extend(e).withLinks(map.get(e.host.canonize).map(_ (entity, e)).getOrElse(Nil))

  private val artistClass = classOf[Artist]
  private val albumClass = classOf[Album]
  def apply[R <: Reconcilable : Manifest](entity: R, e: MarkedLink[R]): ExtendedLink[R] = {
    val map = implicitly[Manifest[R]].runtimeClass match {
      case `artistClass` => artistExtensions
      case `albumClass` => albumExtensions
    }
    auxExtend(entity, e, map.asInstanceOf[HostMap[LinkExtender[R]]])
  }
  def apply[R <: Reconcilable : Manifest](entity: R, e: TimestampedLinks[R]): TimestampedExtendedLinks[R] =
    TimestampedExtendedLinks(e.links.map(apply[R](entity, _)), e.timestamp)
}

private[external] object CompositeExtender {
  lazy val default =
    new CompositeExtender(
      Map(Host.MusicBrainz -> MusicBrainzExtender,
        Host.AllMusic -> AllMusicArtistExtender,
        Host.LastFm -> LastFmArtistExtender),
      Map(Host.MusicBrainz -> MusicBrainzExtender,
        Host.AllMusic -> AllMusicAlbumExtender))
}
