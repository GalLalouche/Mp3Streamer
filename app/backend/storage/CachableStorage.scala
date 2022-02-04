package backend.storage

import scala.concurrent.{ExecutionContext, Future}

import scalaz.ListT

import common.storage.Storage

trait CachableStorage[Key, Value] extends Storage[Key, Value] {
  def cachedStorage: Future[PartialFunction[Key, Value]]
  def cachedKeys: Future[Key => Boolean]
}
