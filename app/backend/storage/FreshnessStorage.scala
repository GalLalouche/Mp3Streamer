package backend.storage

import backend.FutureOption

import scala.concurrent.Future

import cats.data.OptionT

trait FreshnessStorage[Key, Value] {
  def freshness(k: Key): FutureOption[Freshness]
  /** Throws on repeats. */
  def foreverFresh(k: Key, v: Value): Future[Unit]
  // TODO extract a common parent to this and Storage
  def store(k: Key, v: Value): Future[Unit]
  def update(k: Key, v: Value): OptionT[Future, Value]
  // TODO updateForeverFresh to avoid delete >> store?
  def replace(k: Key, v: Value): FutureOption[Value]
  def load(k: Key): FutureOption[Value]
}
