package common.storage

import scala.concurrent.Future

trait LocalStorage[Key, Value] {
  def store(k: Key, v: Value): Future[Unit]
  def load(k: Key): Future[Value]
}
