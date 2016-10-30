package backend.recon

import backend.configs.Configuration
import backend.storage.SlickStorageUtils
import common.rich.RichT._

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

abstract class SlickReconStorage[K <: Reconcilable](implicit c: Configuration,
                                                    m: Manifest[K]) extends ReconStorage[K]
    with ToFunctorOps with FutureInstances {
  import c.driver.api._

  private class Rows(tag: Tag) extends Table[(String, Option[String], Boolean)](tag, m.runtimeClass.getSimpleName.replaceAll("\\$", "") + "S") {
    def name = column[String]("KEY", O.PrimaryKey)
    def reconId = column[Option[String]]("RECON_ID")
    def isIgnored = column[Boolean]("IS_IGNORED", O.Default(false))
    def * = (name, reconId, isIgnored)
  }
  private val rows = TableQuery[Rows]
  private val db = c.db
  def store(k: K, id: Option[String]): Future[Boolean] =
    store(k, id.map(ReconID.apply) -> (false == id.isDefined))
  /** If an existing value exists, override it. */
  override protected def internalForceStore(k: K, value: (Option[ReconID], Boolean)): Future[Unit] =
  db.run(rows.insertOrUpdate(k.normalize, value._1.map(_.id), value._2)) >| Unit
  /** Returns the value associated with the key, if one exists, or None. */
  override def load(k: K): Future[Option[(Option[ReconID], Boolean)]] =
  db.run(rows
      .filter(_.name === k.normalize)
      .map(e => e.isIgnored -> e.reconId)
      .result
      .map(_.headOption.map(_.swap.mapTo(e => e._1.map(ReconID) -> e._2))))
  override def internalDelete(k: K): Future[Unit] =
    db.run(rows.filter(_.name === k.normalize).delete).>|(Unit)
  override def utils = SlickStorageUtils(c)(rows)
}

class ArtistReconStorage(implicit c: Configuration) extends SlickReconStorage[Artist]
class AlbumReconStorage(implicit c: Configuration) extends SlickReconStorage[Album]
