package common.io.avro

import java.time.{Instant, LocalDateTime, ZoneOffset}

import alleycats.Zero
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.GenericRecord
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec

import common.AvroableSaver
import common.concurrency.ActorState
import common.test.AsyncAuxSpecs

class AvroableCOWImplTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private val factory = injector.instance[AvroableCOWFactory]
  private implicit val LongAvroable: Avroable[Long] = new Avroable[Long] {
    override val schema: Schema = SchemaBuilder.record("LongWrapper").fields().requiredLong("value").endRecord()
    override def toRecord(a: Long): GenericRecord = {
      val r = new Record(schema)
      r.put("value", a)
      r
    }
    override def fromRecord(r: GenericRecord): Long = r.get("value").asInstanceOf[Long]
  }
  private implicit val zeroLong: Zero[Long] = alleycats.Zero[Long](0L)
  private def $ : ActorState[String, LocalDateTime] = factory[String, Long, LocalDateTime](
    LocalDateTime.parse(_).atZone(ZoneOffset.UTC).toInstant.toEpochMilli,
    Instant.ofEpochMilli(_).atZone(ZoneOffset.UTC).toLocalDateTime,
  )

  "initial value" - {
    "empty" in {
      $.get shouldReturn LocalDateTime.parse("1970-01-01T00:00:00")
    }
    "nonEmpty" in {
      injector.instance[AvroableSaver].save(Seq(42_000L))
      $.get shouldReturn LocalDateTime.parse("1970-01-01T00:00:42")
    }
  }

  "set then get" in {
    val s = "1970-01-01T00:42:00"
    val original = $
    val expected = LocalDateTime.parse(s)
    original.set(s).map { oldValue =>
      oldValue shouldReturn expected
      original.get shouldReturn expected
      $.get shouldReturn expected
    }
  }
}
