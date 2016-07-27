package backend

trait StringSerializable[T] {
  def encode(t: T): String
  def decode(s: String): T
}
