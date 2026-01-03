package backend.new_albums.filler

import backend.recon.{Reconcilable, Reconciler, ReconID, ReconStorage}
import backend.recon.StoredReconResult.HasReconResult
import rx.lang.scala.Observable
import rx.lang.scala.schedulers.ImmediateScheduler

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFlatMapOps
import common.rich.func.kats.ObservableInstances._

import common.concurrency.SimpleActor
import common.rich.RichFuture._
import common.rich.primitives.RichBoolean.richBoolean
import common.rx.RichObservable
import common.rx.RichObservable._
import common.rx.RichObservableSpecVer.richObservableSpecVer

private class ReconFiller[R <: Reconcilable](
    reconciler: Reconciler[R],
    storage: ReconStorage[R],
    aux: ReconFillerAux[R],
)(implicit ec: ExecutionContext) {
  private val cache = storage.cachedKeys.get
  private val storer = SimpleActor.async[(R, ReconID)](
    "storer",
    { case (r, recondID) =>
      scribe.info(
        s"Storing <${aux.prettyPrint(r)}>: https://musicbrainz.org/${aux.musicBrainzPath}/${recondID.id}",
      )
      storage.store(r, HasReconResult(recondID, isIgnored = false))
    },
  )

  private def go(r: R): Observable[ReconID] =
    RichObservable.from(reconciler(r)).filterFuture(aux.verify(r, _))
  private def newRecons(rs: Iterable[R]): Observable[(R, ReconID)] = {
    def hasNoRecon(r: R): Boolean = cache(r).isFalse
    // Taking only a partial amount to avoid DOSing musicbrainz and also make manual fixing more manageable.
    Observable.from(rs.filter(hasNoRecon)).take(10).mproduct(go)
  }

  def go(rs: Iterable[R]): Future[_] =
    newRecons(rs).observeOn(ImmediateScheduler()).doOnEach(storer ! _).toFuture
}
