package backend.external.extensions

import backend.external._
import backend.logging.LoggerProvider
import backend.recon.{Album, Artist, Reconcilable}
import common.rich.collections.RichTraversableOnce._
import common.rich.func.ToMoreFoldableOps

import scalaz.std.OptionInstances

private[external] class CompositeExtender private(
    artistExtenders: Seq[LinkExtender[Artist]],
    albumExtenders: Seq[LinkExtender[Album]])
    extends ToMoreFoldableOps with OptionInstances {
  private val artistExtendersMap: HostMap[LinkExtender[Artist]] =
    artistExtenders.mapBy(_.host)
  private val albumExtenderMap: HostMap[LinkExtender[Album]] =
    albumExtenders.mapBy(_.host)

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

private[external] object CompositeExtender {
  def default(implicit lp: LoggerProvider) =
    new CompositeExtender(
      Seq(MusicBrainzArtistExtender, new AllMusicArtistExtender, new LastFmArtistExtender),
      Seq(MusicBrainzAlbumExtender, new AllMusicAlbumExtender))
}
