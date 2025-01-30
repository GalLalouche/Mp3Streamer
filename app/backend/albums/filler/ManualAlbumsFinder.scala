package backend.albums.filler

import javax.inject.{Inject, Singleton}

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import models.MusicFinder

import scala.concurrent.Future

import common.TimedLogger
import common.concurrency.SimpleTypedActor
import common.io.DirectoryRef
import common.rich.RichT.richT

@Singleton private class ManualAlbumsFinder @Inject() (
    timed: TimedLogger,
    mf: MusicFinder,
    reconcilableFactory: ReconcilableFactory,
) extends SimpleTypedActor[Artist, Option[Set[Album]]] {
  override def !(m: => Artist): Future[Option[Set[Album]]] = delegate ! m

  private val delegate = SimpleTypedActor("ManualAlbumsFinder", fallback)

  private def fallback(artist: Artist): Option[Set[Album]] = timed(
    s"Cannot find directory for <$artist>, falling back to manual album search",
    scribe.warn(_),
  ) {
    mf.albumDirs
      .filter(e => mf.getSongsInDir(e).head.artist == artist)
      .map((e: DirectoryRef) => reconcilableFactory.toAlbum(e).get)
      .toSet
      .optFilter(_.nonEmpty)
  }
}
