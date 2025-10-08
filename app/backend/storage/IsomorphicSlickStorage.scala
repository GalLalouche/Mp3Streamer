package backend.storage

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.MoreSeqInstances._
import scalaz.ListT
import scalaz.Scalaz.ToFoldableOps

import common.ds.RichIList.richIList
import common.rich.RichT.richT

// TODO too many damn inheritances, use type clasess?
abstract class IsomorphicSlickStorage[Key, Value](implicit ec: ExecutionContext, dbP: DbProvider)
    extends SlickSingleKeyColumnStorageTemplateFromConf[Key, Value](ec, dbP)
    with CachableStorage[Key, Value] {
  protected def extractKey(e: Entity): Key
  import profile.api._
  // TODO ListT(db.run(tableQuery.result).map(_.toList)) can be moved to SlickStorageTemplate
  def loadAllPairs: ListT[Future, (Key, Value)] =
    ListT(db.run(tableQuery.result).map(_.toIList)).map(_.toTuple(extractKey, extractValue))
  // FIXME this is wrong, since it doesn't normalized
  override def cachedStorage: Future[Map[Key, Value]] = loadAllPairs.run.map(_.toFuckingMap)
  override def cachedKeys: Future[Set[Key]] = cachedStorage.map(_.keySet)
}
