package backend.logging

import org.scalatest.{FreeSpec, ShouldMatchers}

class StringBuilderLoggerTest extends FreeSpec with ShouldMatchers {
  "append to the builder" in {
    val sb = new StringBuilder
    val $ = new StringBuilderLogger(sb)
    $.log("test", LoggingLevel.Info)
    sb.toString should include ("test")
  }
}
