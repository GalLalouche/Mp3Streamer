package backend.storage

import backend.FutureOption

import scala.concurrent.Future

import scalaz.OptionT

trait FreshnessStorage[Key, Value] {
  def freshness(k: Key): FutureOption[Freshness]
  def storeWithoutTimestamp(k: Key, v: Value): Future[Unit]
  // TODO extract a common parent to this and Storage
  def update(k: Key, v: Value): OptionT[Future, Value]
  def replace(k: Key, v: Value): OptionT[Future, Value]
  def load(k: Key): FutureOption[Value]
}
