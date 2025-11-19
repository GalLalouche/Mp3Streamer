package backend.storage

import scala.concurrent.{ExecutionContext, Future}

import cats.implicits.toFunctorOps
import common.rich.func.kats.Nesteds.SeqT

import common.rich.RichT.richT

// TODO too many damn inheritances, use type clasess?
abstract class IsomorphicSlickStorage[Key, Value](implicit ec: ExecutionContext, dbP: DbProvider)
    extends SlickSingleKeyColumnStorageTemplateFromConf[Key, Value](ec, dbP)
    with CachableStorage[Key, Value] {
  protected def extractKey(e: Entity): Key
  def loadAllPairs: SeqT[Future, (Key, Value)] =
    loadAll.map(_.toTuple(extractKey, extractValue))
  // FIXME this is wrong, since it isn't normalized
  override def cachedStorage: Future[Map[Key, Value]] = loadAllPairs.value.map(_.toMap)
  override def cachedKeys: Future[Set[Key]] = cachedStorage.map(_.keySet)
}
