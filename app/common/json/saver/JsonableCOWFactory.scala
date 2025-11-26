package common.json.saver

import alleycats.Zero
import com.google.inject.Inject

import common.concurrency.ActorState
import common.json.Jsonable

/**
 * {{{
 *  ___________________________
 * < Writes to JSON on changes >
 *  ---------------------------
 *         \   ^__^
 *          \  (oo)\_______
 *             (__)\       )\/\
 *                 ||----w |
 *                 ||     ||
 * }}}
 *
 * On [[set]], saves itself using `JsonableSaver`. On initial [[get]], returns the loaded value.
 */
class JsonableCOWFactory @Inject() (saver: JsonableSaver) {
  def apply[Input, Internal: JsonableSaveable: Manifest, Output](
      i2i: Input => Internal,
      i2o: Internal => Output,
  ): ActorState[Input, Output] = new JsonableCOWImpl[Input, Internal, Output](saver, i2i, i2o)
  /**
   * A very simplified overload of the above, when there is intermediate internal type, and there is
   * no need for special [[JsonableSaver]] loading and saving logic.
   */
  def apply[A <: AnyRef: Manifest: Jsonable: Zero]: ActorState[A, A] =
    new JsonableCOWImpl[A, A, A](saver, identity, identity)
}
