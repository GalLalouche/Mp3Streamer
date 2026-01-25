package common.io.avro

import java.lang

import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.util.Utf8

import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.IterableHasSeqStream

object RichAvro {
  // TODO reduce duplication of handling nulls
  implicit class richGenericRecord(private val $ : GenericRecord) extends AnyVal {
    def getString(key: String): String = {
      val res = getStringNullable(key)
      if (res == null)
        throw new NoSuchElementException(s"Field '$key' is null")
      res
    }
    def optString(key: String): Option[String] = Option(getStringNullable(key))
    private def getStringNullable(key: String): String =
      $.get(key) match {
        case null => null
        case u: Utf8 => u.toString
        case s: String => s
        case _ =>
          throw new IllegalStateException(
            s"Field '$key' is of unexpected type: ${$.get(key).getClass}",
          )
      }
    def getInt(key: String): Int = $.get(key).asInstanceOf[Int]
    def optInt(key: String): Option[Int] = $.get(key) match {
      case null => None
      case i: java.lang.Integer => Some(i)
      case _ =>
        throw new IllegalStateException(
          s"Field '$key' is of unexpected type: ${$.get(key).getClass}",
        )
    }
    def getLong(key: String): Long = $.get(key).asInstanceOf[Long]
    def getDouble(key: String): Double = $.get(key).asInstanceOf[Double]
    def optDouble(key: String): Option[Double] = $.get(key) match {
      case null => None
      case d: java.lang.Double => Some(d)
      case _ =>
        throw new IllegalStateException(
          s"Field '$key' is of unexpected type: ${$.get(key).getClass}",
        )
    }
    def getArray[T <: AnyRef: Avroable: Manifest](key: String): Array[T] = $.get(key) match {
      case null => throw new NoSuchElementException(s"No value under key: '$key'")
      case i: lang.Iterable[GenericRecord] => i.asScala.map(Avroable[T].fromRecord).toArray
      case i: Iterable[GenericRecord] => i.view.map(Avroable[T].fromRecord).toArray
      case other =>
        throw new IllegalStateException(
          s"Field '$key' is of unexpected type: ${other.getClass}",
        )
    }
    def parseInner[A: AvroReadable](key: String): A =
      AvroReadable[A].fromRecord($.get(key).asInstanceOf[GenericRecord])
  }
  implicit class richGenericDataRecord(private val $ : GenericData.Record) extends AnyVal {
    def putOpt(key: String, value: Option[Any]): Unit = $.put(key, value.orNull)
  }
  implicit class AvroableSeqOps[A: Avroable](private val as: Iterable[A]) {
    private val avro = Avroable[A]
    def toDataArray: GenericData.Array[GenericRecord] = new GenericData.Array(
      SchemaBuilder.array.items(avro.schema),
      as.asJavaSeqStream.map(avro.toRecord).toList,
    )
  }
}
