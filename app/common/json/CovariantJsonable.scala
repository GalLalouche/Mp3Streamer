package common.json

trait CovariantJsonable[A, B <: A] extends JsonWriteable[A] with JsonReadable[B]
