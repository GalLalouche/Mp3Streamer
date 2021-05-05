package backend.albums.filler

import backend.recon.{Reconcilable, Reconciler, ReconID, ReconStorage}
import backend.recon.StoredReconResult.HasReconResult
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ImmediateScheduler

import scala.concurrent.{ExecutionContext, Future}

import scalaz.syntax.bind.ToBindOps
import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreObservableInstances._
import common.rich.func.ToMoreFunctorOps._

import common.concurrency.SimpleActor
import common.rich.RichObservable
import common.rich.RichObservable._

private class ReconFiller[R <: Reconcilable](
    reconciler: Reconciler[R],
    storage: ReconStorage[R],
    aux: ReconFillerAux[R],
)(implicit ec: ExecutionContext) {
  private val storer = SimpleActor.async[(R, ReconID)]("storer", {case (r, recondID) =>
    println(s"Storing <${aux.prettyPrint(r)}>: https://musicbrainz.org/${aux.name}/${recondID.id}")
    storage.store(r, HasReconResult(recondID, isIgnored = false))
  })

  private def go(r: R): Observable[ReconID] =
    RichObservable.from(reconciler(r)).filterFuture(aux.verify(r, _))
  private def newRecons(rs: Iterable[R]): Observable[(R, ReconID)] = {
    def hasNoRecon(r: R): Future[Boolean] = storage.exists(r).negated
    Observable.from(rs).filterFuture(hasNoRecon).mproduct(go)
  }

  def go(rs: Iterable[R]): Future[_] =
    newRecons(rs).observeOn(ImmediateScheduler()).doOnEach(storer ! _).toFuture
}
