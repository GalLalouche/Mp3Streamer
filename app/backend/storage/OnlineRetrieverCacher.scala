package backend.storage

import backend.Retriever

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import cats.implicits.toFlatMapOps
import common.rich.func.kats.RichOptionT._
import common.rich.func.kats.ToMoreMonadErrorOps._

import common.rich.RichFuture.richFuture
import common.storage.{Storage, StoreMode}

/**
 * Tries to first retrieve information from a local repository. If it fails, it will try to use an
 * online API, and save the result persistently.
 */
class OnlineRetrieverCacher[Key, Value](
    localStorage: Storage[Key, Value],
    onlineRetriever: Retriever[Key, Value],
)(implicit ec: ExecutionContext)
    extends Storage[Key, Value] {
  /**
   * Returns [[Failure]] if the online retrieval failed. Storage failed are dealt with in the
   * storage layer.
   */
  def apply(k: Key): Future[Try[Value]] = (localStorage.load(k) |||| onlineRetriever(k).flatTap(
    localStorage
      .store(k, _)
      .mapError(
        new Exception("Cacher failed to write recon. This usually indicates a race condition.", _),
      ),
  )).toTry
  // delegate all methods to localStorage
  // Use explicit type for implicit inference
  override def update(k: Key, v: Value) = localStorage.update(k, v)
  override def replace(k: Key, v: Value) = localStorage.replace(k, v)
  override def store(k: Key, v: Value) = localStorage.store(k, v)
  override def storeMultiple(kvs: Iterable[(Key, Value)]) = localStorage.storeMultiple(kvs)
  override def overwriteMultipleVoid(kvs: Iterable[(Key, Value)]) =
    localStorage.overwriteMultipleVoid(kvs)
  override def mapStore(mode: StoreMode, k: Key, f: Value => Value, default: => Value) =
    localStorage.mapStore(mode, k, f, default)
  override def load(k: Key) = localStorage.load(k)
  override def exists(k: Key) = localStorage.exists(k)
  override def delete(k: Key) = localStorage.delete(k)
  override def deleteAll(ks: Iterable[Key]) = localStorage.deleteAll(ks)
  override def utils = localStorage.utils
}
