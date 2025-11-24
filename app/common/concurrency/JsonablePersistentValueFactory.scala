package common.concurrency

import com.google.inject.Inject

import common.io.JsonableSaver
import common.json.Jsonable

class JsonablePersistentValueFactory @Inject() (saver: JsonableSaver) {
  def apply[A <: AnyRef: Manifest: Jsonable](default: => A) =
    new JsonablePersistentValue[A](saver, default)
}
