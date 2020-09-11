package backend.storage

import backend.FutureOption

import scala.concurrent.Future

import common.storage.Storage

trait FreshnessStorage[Key, Value] extends Storage[Key, Value] {
  def freshness(k: Key): FutureOption[Freshness]
  def storeWithoutTimestamp(k: Key, v: Value): Future[Unit]
}
