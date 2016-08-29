package backend.external

import backend.recon.{Album, Artist, Reconcilable}
import backend.storage.Retriever
import common.io.InternetTalker

import scala.concurrent.{ExecutionContext, Future}

class CompositeReconciler[R <: Reconcilable] private(rs: Reconciler[R]*)(implicit ec: ExecutionContext, it: InternetTalker)
    extends Retriever[R, Links[R]] {
  override def apply(v1: R): Future[Links[R]] = Future sequence rs.map(_ (v1)) map (_.flatten)
}
object CompositeReconciler {
  def artist(implicit ec: ExecutionContext, it: InternetTalker) = new CompositeReconciler[Artist](new LastFmReconciler())
  def album(implicit ec: ExecutionContext, it: InternetTalker) = new CompositeReconciler[Album]()
}
