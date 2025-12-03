package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.{Inject, Singleton}
import musicfinder.{MusicFinder, SongDirectoryParser}

import scala.concurrent.Future

import cats.implicits.toBifunctorOps

import common.TimedLogger
import common.concurrency.SimpleTypedActor
import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.primitives.RichEither.richEitherString

@Singleton private class ManualAlbumsFinder @Inject() (
    timed: TimedLogger,
    mf: MusicFinder,
    songDirectoryParser: SongDirectoryParser,
    reconcilableFactory: ReconcilableFactory,
) extends SimpleTypedActor[Artist, Option[Set[Album]]] {
  override def !(m: => Artist): Future[Option[Set[Album]]] = delegate ! m

  private val delegate = SimpleTypedActor("ManualAlbumsFinder", fallback)

  private def fallback(artist: Artist): Option[Set[Album]] = timed(
    s"Cannot find directory for <$artist>, falling back to manual album search",
    scribe.warn(_),
  ) {
    mf.albumDirs
      .filter(songDirectoryParser(_).head.artist == artist)
      .map((e: DirectoryRef) => reconcilableFactory.toAlbum(e).leftMap(_.toString).getOrThrow)
      .toSet
      .optFilter(_.nonEmpty)
  }
}
