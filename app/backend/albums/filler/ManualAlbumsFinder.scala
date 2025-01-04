package backend.albums.filler

import javax.inject.{Inject, Singleton}

import backend.recon.{Album, ReconcilableFactory}
import models.MusicFinder
import models.TypeAliases.ArtistName

import scala.concurrent.Future

import common.TimedLogger
import common.concurrency.SimpleTypedActor
import common.io.DirectoryRef
import common.rich.RichT.richT

@Singleton private class ManualAlbumsFinder @Inject() (
    timed: TimedLogger,
    mf: MusicFinder,
    reconcilableFactory: ReconcilableFactory,
) extends SimpleTypedActor[ArtistName, Option[Set[Album]]] {
  override def !(m: => ArtistName): Future[Option[Set[Album]]] = delegate ! m

  private val delegate = SimpleTypedActor("ManualAlbumsFinder", fallback)

  private def fallback(artistName: ArtistName): Option[Set[Album]] = timed(
    s"Cannot find directory for <$artistName>, falling back to manual album search",
    scribe.warn(_),
  ) {
    mf.albumDirs
      .filter(e => mf.getSongsInDir(e).head.artistName.toLowerCase == artistName)
      .map((e: DirectoryRef) => reconcilableFactory.toAlbum(e).get)
      .toSet
      .optFilter(_.nonEmpty)
  }
}
