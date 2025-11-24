package common.json.saver

import java.time.{Instant, LocalDateTime, ZoneOffset}

import alleycats.Zero
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.OneInstancePerTest
import org.scalatest.freespec.AsyncFreeSpec
import play.api.libs.json.{JsNumber, JsValue}

import common.json.Jsonable
import common.test.AsyncAuxSpecs

class JsonableCOWImplTest extends AsyncFreeSpec with AsyncAuxSpecs with OneInstancePerTest {
  private val injector = TestModuleConfiguration().injector
  private val factory = injector.instance[JsonableCOWFactory]
  private implicit val LongJsonsable: Jsonable[Long] = new Jsonable[Long] {
    override def jsonify(a: Long): JsValue = JsNumber(a)
    override def parse(json: JsValue): Long = json.as[Long]
  }
  private implicit val zeroLong: Zero[Long] = alleycats.Zero[Long](0L)
  private def $ : JsonableCOW[String, LocalDateTime] = factory[String, Long, LocalDateTime](
    LocalDateTime.parse(_).atZone(ZoneOffset.UTC).toInstant.toEpochMilli,
    Instant.ofEpochMilli(_).atZone(ZoneOffset.UTC).toLocalDateTime,
  )

  "initial value" - {
    "empty" in {
      $.get shouldReturn LocalDateTime.parse("1970-01-01T00:00:00")
    }
    "nonEmpty" in {
      injector.instance[JsonableSaver].saveObject[Long](42_000L)
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
