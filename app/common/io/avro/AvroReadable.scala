package common.io.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord

import scala.util.Try

trait AvroReadable[A] {
  def fromRecord(r: GenericRecord): A
  def schema: Schema
}

object AvroReadable {
  def apply[A: AvroReadable]: AvroReadable[A] = implicitly[AvroReadable[A]]

  implicit def tryAvroable[A: AvroReadable]: AvroReadable[Try[A]] = new AvroReadable[Try[A]] {
    override def schema: Schema = AvroReadable[A].schema
    override def fromRecord(r: GenericRecord): Try[A] = Try(AvroReadable[A].fromRecord(r))
  }
}
