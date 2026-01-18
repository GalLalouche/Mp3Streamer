package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.{Inject, Singleton}
import musicfinder.{MusicFiles, SongDirectoryParser}

import scala.concurrent.Future

import cats.implicits.toBifunctorOps

import common.TimedLogger
import common.concurrency.SimpleTypedActor
import common.rich.RichT.richT
import common.rich.primitives.RichEither.richEitherString
import common.rx.RichObservable.richObservable

@Singleton private class ManualAlbumsFinder @Inject() (
    timed: TimedLogger,
    mf: MusicFiles,
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
      .filter(songDirectoryParser(_).next().artist == artist)
      .map(reconcilableFactory.toAlbum(_).leftMap(_.toString).getOrThrow)
      .buildBlocking(Set.newBuilder)
      .optFilter(_.nonEmpty)
  }
}
