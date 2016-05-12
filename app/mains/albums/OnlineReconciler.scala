package mains.albums


import org.joda.time.LocalDate

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait OnlineReconciler extends (String => Future[Option[ID]]) {
  def apply(artistName: String): Future[Option[ID]] = recon(artistName).map(Some.apply)
  def recon(artistName: String): Future[ID]
  def getAlbumsMetadata(key: ID): Future[Seq[(LocalDate, String)]]
}
