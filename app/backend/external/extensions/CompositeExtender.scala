package backend.external.extensions

import backend.external._
import backend.recon.{Album, Artist, Reconcilable}
import com.google.inject.Inject

import common.rich.func.ToMoreFoldableOps._
import scalaz.std.option.optionInstance

import common.rich.collections.RichTraversableOnce._

private[external] class CompositeExtender @Inject() (
    allMusicArtistExtender: AllMusicArtistExtender,
    lastFmArtistExtender: LastFmArtistExtender,
    allMusicAlbumExtender: AllMusicAlbumExtender,
) {
  private val artistExtendersMap: HostMap[LinkExtender[Artist]] =
    Vector(allMusicArtistExtender, lastFmArtistExtender, MusicBrainzArtistExtender).mapBy(_.host)
  private val albumExtenderMap: HostMap[LinkExtender[Album]] =
    Vector(allMusicAlbumExtender, MusicBrainzAlbumExtender).mapBy(_.host)

  private val artistClass = classOf[Artist]
  private val albumClass = classOf[Album]
  private def extendLink[R <: Reconcilable: Manifest](
      entity: R,
      link: MarkedLink[R],
      allLinks: MarkedLinks[R],
  ): ExtendedLink[R] = {
    val map: HostMap[LinkExtender[R]] = (manifest.runtimeClass match {
      case `artistClass` => artistExtendersMap
      case `albumClass` => albumExtenderMap
    }).asInstanceOf[HostMap[LinkExtender[R]]]
    val extendedLinks = map.get(link.host).mapHeadOrElse(_.extend(entity, allLinks), Nil)
    ExtendedLink.extend(link).withLinks(extendedLinks)
  }
  def apply[R <: Reconcilable: Manifest](
      entity: R,
      e: TimestampedLinks[R],
  ): TimestampedExtendedLinks[R] =
    TimestampedExtendedLinks(e.links.map(extendLink(entity, _, e.links)), e.timestamp)
}
