package backend.recon

import scala.concurrent.Future

trait OnlineReconciler[Key] extends (Key => Future[Option[ReconID]]) {
  override def apply(k: Key): Future[Option[ReconID]]
}
