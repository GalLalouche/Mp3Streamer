package common.concurrency

import scala.concurrent.Future

import common.io.JsonableSaver
import common.json.Jsonable
import common.rich.RichT._

class JsonablePersistentValue[A <: AnyRef: Manifest: Jsonable] private[concurrency] (
    saver: JsonableSaver,
    default: => A,
) {
  def get: A = value
  def set(newValue: A): Future[A] = actor ! newValue
  def modify(f: A => A): Future[A] = set(f(get))

  private var value = saver.loadObjectOpt[A].getOrElse(default)
  private val actor = SimpleTypedActor[A, A](
    s"JsonablePersistentValue <${manifest.runtimeClass.simpleName}>",
    newValue => {
      if (newValue.neq(value)) {
        value = newValue
        saver.saveObject(value)
      }
      value
    },
  )
}
