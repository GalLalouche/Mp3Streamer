package common.json.saver

import alleycats.Zero

import common.json.Jsonable

trait JsonableSaveable[A] {
  def save(saver: JsonableSaver, a: A): Unit
  def load(saver: JsonableSaver): A
}

object JsonableSaveable {
  implicit def fromZeroJsonable[A: Manifest: Zero](implicit ev: Jsonable[A]): JsonableSaveable[A] =
    new JsonableSaveable[A] {
      override def save(saver: JsonableSaver, a: A): Unit = saver.saveObject(a)
      override def load(saver: JsonableSaver): A = saver.loadObjectOpt[A].getOrElse(Zero[A].zero)
    }
  def fromJsonableLenient[A: Manifest: Jsonable]: JsonableSaveable[Seq[A]] =
    new JsonableSaveable[Seq[A]] {
      override def save(saver: JsonableSaver, a: Seq[A]): Unit = saver.saveArray(a)
      override def load(saver: JsonableSaver): Seq[A] = saver.loadArrayHandleErrors[A]._1
    }
}
