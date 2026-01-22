package backend.new_albums.filler

import backend.recon.{Album, Artist, ReconcilableFactory}
import backend.recon.Reconcilable.SongExtractor
import com.google.inject.{Inject, Singleton}
import models.{ArtistDir, ArtistDirFactory}
import musicfinder.{ArtistNameNormalizer, MusicFiles, SongDirectoryParser}
import rx.lang.scala.Observable

import scala.concurrent.Future

import cats.implicits.toBifunctorOps
import common.rich.func.kats.ToMoreFoldableOps.toMoreFoldableOps

import common.TimedLogger
import common.concurrency.SimpleTypedActor
import common.io.DirectoryRef
import common.rich.RichT.richT
import common.rich.primitives.RichEither.richEither
import common.rx.RichObservable.richObservable

// TODO this one is still problematic since it runs everytime a new artist is added. It would be a
//  lot better if whatever called this would wait until the artist are indexed first.
@Singleton private class ManualAlbumsFinder @Inject() (
    timed: TimedLogger,
    mf: MusicFiles,
    songDirectoryParser: SongDirectoryParser,
    artistDirFactory: ArtistDirFactory,
    normalizer: ArtistNameNormalizer,
    reconcilableFactory: ReconcilableFactory,
) extends SimpleTypedActor[Artist, Option[Set[Album]]] {
  override def !(m: => Artist): Future[Option[Set[Album]]] = delegate ! m

  private val delegate = SimpleTypedActor("ManualAlbumsFinder", fallback)

  private def fallback(artist: Artist): Option[Set[Album]] = timed(
    s"Cannot find directory for <$artist>, falling back to manual album search",
    scribe.warn(_),
  ) {
    val dirsFromArtistLookup: Observable[Option[Iterable[DirectoryRef]]] =
      artistDir(artist).map(_.map(_.albums.map(_.dir)))
    lazy val dirsFromAlbumLookup: Observable[DirectoryRef] =
      mf.albumDirs.filter(songDirectoryParser(_).next().artist == artist)
    dirsFromArtistLookup
      .flatMap(_.mapHeadOrElse(Observable.from(_), dirsFromAlbumLookup))
      .map(reconcilableFactory.toAlbum(_).leftMap(_.toString).getOrThrow)
      .buildBlocking(Set.newBuilder)
      .optFilter(_.nonEmpty)
  }

  // Searching by Artist is A LOT faster than searching by Album.
  private def artistDir(artist: Artist): Observable[Option[ArtistDir]] = {
    val normalized = normalizer(artist.name)
    def containsSong(ad: ArtistDir): Boolean = (for {
      album <- ad.albums.iterator
      song <- album.songs.iterator
      // TODO ScalaCommon.or/end for unordered foldable.
    } yield song.artist == artist).exists(identity)
    mf.artistDirs
      .filter(_.name == normalized)
      .map(artistDirFactory.fromDir)
      .headOption
      .map(_.filter(containsSong))
  }
}
