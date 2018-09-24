package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.module.TestModuleConfiguration
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import net.codingwell.scalaguice.InjectorExtensions._
import common.AuxSpecs
import org.scalatest.Suite
import common.rich.RichFuture._
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec extends AuxSpecs { self: Suite =>
  protected val injector = TestModuleConfiguration().injector
  protected implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val config = ConfigFactory.load()
      .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
      .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
  private val am = ActorMaterializer()(ActorSystem.create("ControllerSpec-System", config))
  def getBytes(result: Future[Result]): Array[Byte] = result.get.body.consumeData(am).get.toArray
}
