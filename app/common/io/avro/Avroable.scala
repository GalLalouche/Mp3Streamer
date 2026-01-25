package common.io.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord

import common.io.avro.RichAvro.richGenericRecord

trait Avroable[A] extends AvroReadable[A] {
  def toRecord(a: A): GenericRecord
}

object Avroable {
  def apply[A: Avroable]: Avroable[A] = implicitly[Avroable[A]]

  implicit def tuple2Avroable[A: Avroable, B: Avroable]: Avroable[(A, B)] = new Avroable[(A, B)] {
    override lazy val schema: Schema =
      org.apache.avro.SchemaBuilder
        .record("Tuple2")
        .namespace("scala")
        .fields()
        .name("_1")
        .`type`(Avroable[A].schema)
        .noDefault()
        .name("_2")
        .`type`(Avroable[B].schema)
        .noDefault()
        .endRecord()

    override def toRecord(t: (A, B)): GenericRecord = {
      val record = new Record(schema)
      record.put("_1", Avroable[A].toRecord(t._1))
      record.put("_2", Avroable[B].toRecord(t._2))
      record
    }

    override def fromRecord(r: GenericRecord): (A, B) =
      (r.parseInner[A]("_1"), r.parseInner[B]("_2"))
  }
}
