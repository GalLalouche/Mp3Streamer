package backend.albums.filler

import backend.mb.MbArtistReconciler
import backend.recon.{Artist, ArtistReconStorage, ReconID}
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
import common.rich.RichFuture._
import common.rich.RichObservable
import common.rich.RichObservable._

private class ArtistReconFiller @Inject()(
    ea: ExistingAlbums,
    reconciler: MbArtistReconciler,
    storage: ArtistReconStorage,
    verifier: ArtistReconVerifier,
    ec: ExecutionContext,
) {
  private implicit val iec: ExecutionContext = ec

  private val storer = SimpleActor.async[(Artist, ReconID)]("storer", {case (a, r) =>
    println(s"Storing <${a.name}>: https://musicbrainz.org/artist/${r.id}")
    storage.store(a, HasReconResult(r, isIgnored = false))
  })

  private def go(artist: Artist): Observable[ReconID] =
    RichObservable.from(reconciler(artist)).filterFuture(verifier(artist, _))
  private def newRecons: Observable[(Artist, ReconID)] = {
    def hasNoRecon(a: Artist): Future[Boolean] = storage.exists(a).negated
    Observable.from(ea.artists).filterFuture(hasNoRecon).mproduct(go)
  }

  def go(): Future[_] = newRecons.observeOn(ImmediateScheduler()).doOnEach(storer ! _).toFuture
}

private object ArtistReconFiller {
  def main(args: Array[String]): Unit = {
    val injector = ExistingAlbumsModules.overridingStandalone(ExistingAlbumsModules.lazyAlbums)
    implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
    injector.instance[ArtistReconFiller].go().get
  }
}
