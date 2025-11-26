package common.json.saver

import scala.concurrent.Future

import common.concurrency.{ActorState, SimpleTypedActor}
import common.rich.RichT._

/**
 * An implementation that uses some intermediate [[Internal]] type to store the value to JSON.
 *
 * @tparam Input
 *   What the client sets.
 * @tparam Internal
 *   The internal type uses to store and load from JSON.
 * @tparam Output
 *   What the client gets.
 */
private class JsonableCOWImpl[Input, Internal: JsonableSaveable: Manifest, Output](
    saver: JsonableSaver,
    inputToInternal: Input => Internal,
    internalToOutput: Internal => Output,
) extends ActorState[Input, Output] {
  override def get: Output = value
  override def set(newValue: Input): Future[Output] = actor ! newValue

  private var value = internalToOutput(implicitly[JsonableSaveable[Internal]].load(saver))
  private val actor = SimpleTypedActor[Input, Output](
    s"JsonablePersistentValue <${manifest.runtimeClass.simpleName}>",
    newInput => {
      val internal = inputToInternal(newInput)
      val output = internalToOutput(internal)
      if (output != value) {
        value = output
        implicitly[JsonableSaveable[Internal]].save(saver, internal)
      }
      value
    },
  )
}
