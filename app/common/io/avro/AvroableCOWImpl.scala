package common.io.avro

import scala.concurrent.Future

import common.AvroableSaver
import common.concurrency.{ActorState, SimpleTypedActor}

/**
 * An implementation that uses some intermediate [[Internal]] type to store the value to Avro.
 *
 * @tparam Input
 *   What the client sets.
 * @tparam Internal
 *   The internal type uses to store and load from Avro.
 * @tparam Output
 *   What the client gets.
 */
private class AvroableCOWImpl[Input, Internal: AvroableSaveable: Manifest, Output](
    saver: AvroableSaver,
    inputToInternal: Input => Internal,
    internalToOutput: Internal => Output,
) extends ActorState[Input, Output] {
  override def get: Output = value
  override def set(newValue: Input): Future[Output] = actor ! newValue

  @volatile private var value = internalToOutput(implicitly[AvroableSaveable[Internal]].load(saver))
  private val actor = SimpleTypedActor[Input, Output](
    s"AvroablePersistentValue <${manifest.runtimeClass.getSimpleName}>",
    newInput => {
      val internal = inputToInternal(newInput)
      val output = internalToOutput(internal)
      if (output != value) {
        value = output
        implicitly[AvroableSaveable[Internal]].save(saver, internal)
      }
      value
    },
  )
}
