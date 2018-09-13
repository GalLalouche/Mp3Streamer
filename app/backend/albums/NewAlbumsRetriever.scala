package backend.albums

import backend.logging.Logger
import backend.mb.MbArtistReconciler
import backend.mb.MbArtistReconciler.MbAlbumMetadata
import backend.module.StandaloneModule
import backend.recon.{Album, AlbumReconStorage, Artist, IgnoredReconResult, ReconcilerCacher, ReconID}
import backend.recon.Reconcilable.SongExtractor
import backend.recon.StoredReconResult.{HasReconResult, NoRecon}
import com.google.inject.assistedinject.Assisted
import common.io.IODirectory
import common.rich.RichObservable._
import common.rich.RichT._
import common.rich.func.{MoreSeqInstances, MoreTraverseInstances, ToMoreFoldableOps, ToMoreFunctorOps, ToMoreMonadErrorOps, ToTraverseMonadPlusOps}
import javax.inject.Inject
import models.{IOMusicFinder, Song}
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

import scalaz.{-\/, \/-}
import scalaz.std.{FutureInstances, OptionInstances}

private class NewAlbumsRetriever @Inject()(
    albumReconStorage: AlbumReconStorage,
    logger: Logger,
    ec: ExecutionContext,
    meta: MbArtistReconciler,
    @Assisted mf: IOMusicFinder,
    @Assisted reconciler: ReconcilerCacher[Artist],
) extends FutureInstances with MoreTraverseInstances with ToMoreFunctorOps
    with ToTraverseMonadPlusOps with ToMoreMonadErrorOps with MoreSeqInstances
    with ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = ec
  private val log = logger.verbose _
  private def getExistingAlbums: Seq[Album] = mf.genreDirs
      .flatMap(_.deepDirs)
      .flatMap(NewAlbumsRetriever.dirToAlbum(_, mf))
  private def removeIgnoredAlbums(artist: Artist, albums: Seq[MbAlbumMetadata]): Future[Seq[MbAlbumMetadata]] = {
    def toAlbum(album: MbAlbumMetadata) = Album(
      title = album.title, year = album.releaseDate.getYear, artist = artist)
    def isNotIgnored(metadata: MbAlbumMetadata): Future[Boolean] =
      albumReconStorage.isIgnored(toAlbum(metadata))
          .map(_ != IgnoredReconResult.Ignored)
    albums filterTraverse isNotIgnored
  }

  def findNewAlbums: Observable[NewAlbumRecon] = {
    val cache = ArtistLastYearCache from getExistingAlbums
    for {
      artist <- Observable from cache.artists
      reconIdOpt <- Observable from getReconId(artist)
      reconId <- reconIdOpt.mapHeadOrElse(Observable.just(_), Observable.empty)
      result <- findNewAlbums(cache, artist, reconId)
    } yield result
  }

  private def getReconId(artist: Artist): Future[Option[ReconID]] =
    reconciler(artist).mapEitherMessage {
      case NoRecon => -\/("No recon")
      case HasReconResult(reconId, isIgnored) => if (isIgnored) -\/("Ignored") else \/-(reconId)
    }.foldEither(_.fold(e => {
      logger.info(s"Did not fetch albums for artist<${artist.name}>; reason: ${e.getMessage}")
      None
    }, Some.apply))

  private def findNewAlbums(
      cache: ArtistLastYearCache, artist: Artist, reconId: ReconID): Observable[NewAlbumRecon] = {
    logger.debug(s"Fetching new albums for <$artist>")
    val recons: Future[Seq[NewAlbumRecon]] = meta.getAlbumsMetadata(reconId)
        .flatMap(removeIgnoredAlbums(artist, _))
        .map(cache.filterNewAlbums(artist, _))
        .listen(albums => {
          log(s"Finished working on $artist; found ${if (albums.isEmpty) "no" else albums.size} new albums.")
        })
        .listenError {
          case e: FilteredException => log(s"$artist was filtered, reason: ${e.getMessage}")
          case e: Throwable => e.printStackTrace()
        }.orElse(Nil)
    Observable.from(recons).flattenElements
  }
}

private object NewAlbumsRetriever {
  import backend.recon.ArtistReconStorage
  import com.google.inject.Guice
  import net.codingwell.scalaguice.InjectorExtensions._

  import scala.collection.JavaConverters._

  private def dirToAlbum(dir: IODirectory, mf: IOMusicFinder): Option[Album] = dir.files
      .find(_.extension |> mf.extensions)
      .map(_.file)
      .map(Song(_).release)

  def main(args: Array[String]): Unit = {
    val injector = Guice createInjector StandaloneModule
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    val mf = injector.instance[IOMusicFinder]

    val artist: Artist = Artist("At the Gates")
    def cacheForArtist(a: Artist, mf: IOMusicFinder): ArtistLastYearCache = {
      val mf = injector.instance[IOMusicFinder]
      val artistDir = mf.genreDirs
          .flatMap(_.deepDirs)
          .find(_.name.toLowerCase == a.name.toLowerCase)
          .get
      val lastAlbum = artistDir.dirs.mapIf(_.isEmpty).to(Seq(artistDir))
          .flatMap(dirToAlbum(_, mf))
          .maxBy(_.year)

      Seq.apply(lastAlbum) |> ArtistLastYearCache.from
    }
    val reconciler = new ReconcilerCacher(
      injector.instance[ArtistReconStorage], injector.instance[MbArtistReconciler])
    val $ = injector.instance[NewAlbumsRetrieverFactory].apply(reconciler, mf)
    for {
      reconId <- Observable from $.getReconId(artist)
      album <- $.findNewAlbums(cacheForArtist(artist, mf), artist, reconId.get).take(1)
    } yield {
      album.newAlbum.log()
    }
    Thread.getAllStackTraces.keySet.asScala.filterNot(_.isDaemon).foreach(println)
  }
}
