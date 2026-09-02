package common.io.avro

import java.lang

import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.avro.util.Utf8

import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.IterableHasSeqStream

import common.rich.primitives.RichOption.richOption

object RichAvro {
  implicit class richGenericRecord(private val $ : GenericRecord) extends AnyVal {
    def getString(key: String): String = optOrThrow(key, optString)
    def optString(key: String): Option[String] = Option($.get(key) match {
      case null => null
      case u: Utf8 => u.toString
      case s: String => s
      case _ =>
        throw new IllegalStateException(
          s"Field '$key' is of unexpected type: ${$.get(key).getClass}",
        )
    })
    def getInt(key: String): Int = optOrThrow(key, optInt)
    def optInt(key: String): Option[Int] = $.get(key) match {
      case null => None
      case i: java.lang.Integer => Some(i)
      case _ =>
        throw new IllegalStateException(
          s"Field '$key' is of unexpected type: ${$.get(key).getClass}",
        )
    }
    def getLong(key: String): Long = $.get(key).asInstanceOf[Long]
    def getDouble(key: String): Double = optOrThrow(key, optDouble)
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

  private def optOrThrow[A](key: String, f: String => Option[A]): A =
    f(key).getOrThrow(s"No value under key: '$key'")
}
