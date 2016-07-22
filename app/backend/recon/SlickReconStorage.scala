package backend.recon

import common.rich.RichT._
import slick.driver.SQLiteDriver.api._

import scala.concurrent.{ExecutionContext, Future}

abstract class SlickReconStorage[K <: Reconcilable](implicit ec: ExecutionContext, m: Manifest[K]) extends ReconStorage[K] {
  private class Rows(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, m.simpleName.toUpperCase + "S") {
    def name = column[String]("KEY", O.PrimaryKey)
    def reconId = column[Option[String]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  private val rows = TableQuery[Rows]
  private val db = Database.forURL("jdbc:sqlite:d:/media/music/MBRecon.sqlite", driver = "org.sqlite.JDBC")
  def store(k: K, id: Option[String]): Future[Boolean] =
    store(k, id.map(ReconID.apply) -> (false == id.isDefined))
  /** If an existing value exists, override it. */
  override protected def internalForceStore(k: K, value: (Option[ReconID], Boolean)): Future[Unit] =
  db.run(rows.forceInsert(k.normalize, value._1.map(_.id), value._2)).map(e => Unit)
  /** Returns the value associated with the key, if one exists, or None. */
  override def load(k: K): Future[Option[(Option[ReconID], Boolean)]] =
  db.run(rows
      .filter(_.name === k.normalize)
      .map(e => e.isIgnored -> e.reconId)
      .result
      .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
}

class ArtistReconStorage(implicit ec: ExecutionContext) extends SlickReconStorage[Artist]
class AlbumReconStorage(implicit ec: ExecutionContext) extends SlickReconStorage[Album]
