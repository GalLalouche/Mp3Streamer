package common.io.avro

import alleycats.Zero

import common.AvroableSaver

trait AvroableSaveable[A] {
  def save(saver: AvroableSaver, a: A): Unit
  def load(saver: AvroableSaver): A
}

object AvroableSaveable {
  implicit def fromZeroAvroable[A: Manifest: Zero: Avroable]: AvroableSaveable[A] =
    new AvroableSaveable[A] {
      override def save(saver: AvroableSaver, a: A): Unit = saver.save(Seq(a))
      override def load(saver: AvroableSaver): A = saver.load[A].headOption.getOrElse(Zero[A].zero)
    }

  implicit def fromAvroableLenient[A: Manifest: Avroable]: AvroableSaveable[Seq[A]] =
    new AvroableSaveable[Seq[A]] {
      override def save(saver: AvroableSaver, a: Seq[A]): Unit = saver.save(a)
      override def load(saver: AvroableSaver): Seq[A] = saver.loadArrayHandleErrors[A]._1
    }
}
