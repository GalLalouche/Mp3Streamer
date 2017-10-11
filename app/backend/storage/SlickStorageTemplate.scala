package backend.storage

import backend.configs.Configuration
import slick.ast.BaseTypedType

import scala.concurrent.Future
import scalaz.std.FutureInstances
import scalaz.syntax.ToFunctorOps

abstract class SlickStorageTemplate[Key, Value](implicit protected val c: Configuration) extends
    StorageTemplate[Key, Value] with ToFunctorOps with FutureInstances {
  import c.profile.api._

  protected type Id
  protected type Entity
  protected type EntityTable <: Table[Entity]
  protected val tableQuery: TableQuery[EntityTable]
  protected def toEntity(k: Key, v: Value): Entity
  protected def extractId(k: Key): Id
  protected def toId(et: EntityTable): Rep[Id]
  protected def extractValue(e: Entity): Value
  protected implicit def btt: BaseTypedType[Id]
  protected val db = c.db
  /** If a previous value exists, override it. */
  protected def internalForceStore(k: Key, v: Value): Future[_] =
    db.run(tableQuery.insertOrUpdate(toEntity(k, v)))
  protected def internalDelete(k: Key): Future[_] =
    db.run(tableQuery.filter(toId(_) === extractId(k)).delete)
  override def forceStore(k: Key, v: Value): Future[Option[Value]] =
    load(k).flatMap(existing => internalForceStore(k, v).>|(existing))
  override def store(k: Key, v: Value): Future[Boolean] =
    load(k)
        .map(_.isDefined)
        .flatMap(if (_) Future successful false else internalForceStore(k, v).>|(true))
  override def mapStore(k: Key, f: Value => Value, default: => Value) =
    load(k).flatMap(v => forceStore(k, v map f getOrElse default))
  override def delete(k: Key): Future[Option[Value]] =
    for (existing <- load(k); _ <- internalDelete(k)) yield existing
  override def load(k: Key) =
    db.run(tableQuery.filter(toId(_) === extractId(k)).result).map(_.headOption map extractValue)
  override def utils = SlickStorageUtils(c)(tableQuery)
}
