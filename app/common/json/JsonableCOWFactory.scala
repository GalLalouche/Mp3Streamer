package common.json

import alleycats.Zero
import com.google.inject.Inject

import common.io.JsonableSaver

class JsonableCOWFactory @Inject() (saver: JsonableSaver) {
  def apply[Input, Internal: JsonableSaveable: Manifest, Output](
      inputToInternal: Input => Internal,
      internalToOutput: Internal => Output,
  ): JsonableCOW[Input, Output] =
    new JsonableCOWImpl[Input, Internal, Output](saver, inputToInternal, internalToOutput)
  /**
   * A very simplified overload of the above, when there is intermediate internal type, and there is
   * no need for special [[JsonableSaver]] loading and saving logic.
   */
  def apply[A <: AnyRef: Manifest: Jsonable: Zero]: JsonableCOW[A, A] =
    new JsonableCOWImpl[A, A, A](saver, identity, identity)
}
