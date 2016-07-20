//package backend.recon
//
//import common.storage.OnlineRetrieverCacher
//import models.MusicFinder
//
//import scala.concurrent.ExecutionContext
//
//class AlbumReconciler(repo: ReconStorage, online: OnlineReconciler)(implicit ec: ExecutionContext)
//    extends OnlineRetrieverCacher[String, (Option[ID], Boolean)](repo, online(_).map(_ -> false)) {
//
//  def fill(mf: MusicFinder) {
//    ???
//  }
//}
