package backend.albums.filler

import backend.mb.{MbAlbumMetadata, MbArtistReconciler}
import backend.recon.{Album, Artist, ArtistReconStorage, ReconID, StringReconScorer}
import backend.recon.StoredReconResult.HasReconResult
import javax.inject.Inject
import net.codingwell.scalaguice.InjectorExtensions._
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ImmediateScheduler

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreObservableInstances._
import common.rich.func.ToMoreFunctorOps._

import common.concurrency.SimpleActor
import common.rich.primitives.RichBoolean._
import common.rich.RichFuture._
import common.rich.RichObservable
import common.rich.RichObservable._

private class ArtistReconFiller @Inject()(
    ea: ExistingAlbums,
    reconciler: MbArtistReconciler,
    storage: ArtistReconStorage,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  private val storer = SimpleActor.async[(Artist, ReconID)]("storer", {case (a, r) =>
    println(s"Storing <${a.name}>: https://musicbrainz.org/artist/${r.id}")
    storage.store(a, HasReconResult(r, isIgnored = false))
  })

  private def go(artist: Artist): Observable[ReconID] = for {
    reconId <- RichObservable.from(reconciler(artist))
    reconAlbums <- Observable.from(reconciler.getAlbumsMetadata(reconId))
    if ArtistReconFiller.intersects(ea.albums(artist), reconAlbums)
  } yield reconId
  private def newRecons: Observable[(Artist, ReconID)] = {
    def hasNoRecon(a: Artist): Future[Boolean] = storage.exists(a).negated
    Observable.from(ea.artists).filterFuture(hasNoRecon).mproduct(go)
  }

  def go(): Future[_] = newRecons.observeOn(ImmediateScheduler()).doOnEach(storer ! _).toFuture
}

private object ArtistReconFiller {
  private def intersects(album: Set[Album], reconAlbums: Seq[MbAlbumMetadata]): Boolean = {
    val albumTitles = album.map(_.title)
    val $ = reconAlbums.view.map(_.title).exists(t => albumTitles.map(StringReconScorer(_, t)).max > 0.9)
    if ($.isFalse)
      println(s"Could not reconcile ${album.head.artist}")
    $
  }

  def main(args: Array[String]): Unit = {
    val injector = LocalNewAlbumsModule.overridingStandalone(LocalNewAlbumsModule.lazyAlbums)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[ArtistReconFiller].go().get
  }
}
