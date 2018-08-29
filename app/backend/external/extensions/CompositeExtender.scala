package backend.external.extensions

import backend.external._
import backend.recon.{Album, Artist, Reconcilable}
import common.rich.collections.RichTraversableOnce._
import common.rich.func.ToMoreFoldableOps
import javax.inject.Inject

import scalaz.std.OptionInstances

private[external] class CompositeExtender @Inject()(
    allMusicArtistExtender: AllMusicArtistExtender,
    lastFmArtistExtender: LastFmArtistExtender,
    allMusicAlbumExtender: AllMusicAlbumExtender)
    extends ToMoreFoldableOps with OptionInstances {
  private val artistExtendersMap: HostMap[LinkExtender[Artist]] =
    Seq(allMusicArtistExtender, lastFmArtistExtender, MusicBrainzArtistExtender).mapBy(_.host)
  private val albumExtenderMap: HostMap[LinkExtender[Album]] =
    Seq(allMusicAlbumExtender, MusicBrainzAlbumExtender).mapBy(_.host)

  private val artistClass = classOf[Artist]
  private val albumClass = classOf[Album]
  private def extendLink[R <: Reconcilable : Manifest](
      entity: R, link: MarkedLink[R], allLinks: MarkedLinks[R]): ExtendedLink[R] = {
    val map: HostMap[LinkExtender[R]] = (manifest.runtimeClass match {
      case `artistClass` => artistExtendersMap
      case `albumClass` => albumExtenderMap
    }).asInstanceOf[HostMap[LinkExtender[R]]]
    val extendedLinks = map.get(link.host).mapHeadOrElse(_.apply(entity, allLinks), Nil)
    ExtendedLink.extend(link).withLinks(extendedLinks)
  }
  def apply[R <: Reconcilable : Manifest](entity: R, e: TimestampedLinks[R]): TimestampedExtendedLinks[R] =
    TimestampedExtendedLinks(e.links.map(extendLink(entity, _, e.links)), e.timestamp)
}
