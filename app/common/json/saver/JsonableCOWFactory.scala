package common.json.saver

import alleycats.Zero
import com.google.inject.Inject

import common.json.Jsonable

class JsonableCOWFactory @Inject() (saver: JsonableSaver) {
  def apply[Input, Internal: JsonableSaveable: Manifest, Output](
      i2i: Input => Internal,
      i2o: Internal => Output,
  ): JsonableCOW[Input, Output] = new JsonableCOWImpl[Input, Internal, Output](saver, i2i, i2o)
  /**
   * A very simplified overload of the above, when there is intermediate internal type, and there is
   * no need for special [[JsonableSaver]] loading and saving logic.
   */
  def apply[A <: AnyRef: Manifest: Jsonable: Zero]: JsonableCOW[A, A] =
    new JsonableCOWImpl[A, A, A](saver, identity, identity)
}
