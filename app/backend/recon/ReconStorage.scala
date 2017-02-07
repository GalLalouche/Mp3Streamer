package backend.recon

import backend.storage.StorageTemplate

import scala.concurrent.{ExecutionContext, Future}

abstract class ReconStorage[Key <: Reconcilable](implicit ec: ExecutionContext) extends StorageTemplate[Key, (Option[ReconID], Boolean)] {
  def isIgnored(k: Key): Future[Option[Boolean]] = load(k).map(_.map(_._2))
}
