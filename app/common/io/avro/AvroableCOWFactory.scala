package common.io.avro

import alleycats.Zero
import com.google.inject.Inject

import common.AvroableSaver
import common.concurrency.ActorState

/**
 * {{{
 *  ___________________________
 * < Writes to Avro on changes >
 *  ---------------------------
 *         \   ^__^
 *          \  (oo)\_______
 *             (__)\       )\/
 *                 ||----w |
 *                 ||     ||
 * }}}
 *
 * On [[set]], saves itself using `AvroableSaver`. On initial [[get]], returns the loaded value.
 */
class AvroableCOWFactory @Inject() (saver: AvroableSaver) {
  def apply[Input, Internal: AvroableSaveable: Manifest, Output](
      i2i: Input => Internal,
      i2o: Internal => Output,
  ): ActorState[Input, Output] = new AvroableCOWImpl[Input, Internal, Output](saver, i2i, i2o)
  /**
   * A very simplified overload of the above, when there is intermediate internal type, and there is
   * no need for special [[AvroableSaver]] loading and saving logic.
   */
  def apply[A <: AnyRef: Manifest: Avroable: Zero]: ActorState[A, A] =
    new AvroableCOWImpl[A, A, A](saver, identity, identity)
}
