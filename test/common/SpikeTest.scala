package common

import org.junit.runner.RunWith
import org.specs2.matcher.JsonMatchers
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import akka.actor.{Actor, ActorDSL, ActorSystem}
import akka.pattern.GracefulStopSupport
import akka.util.Timeout._
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import scala.concurrent.duration._
import models.TempDirTest
import models.LogFileManager

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class SpikeTest extends Specification with TempDirTest {

}