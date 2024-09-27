package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import backend.module.TestModuleConfiguration
import com.google.inject
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import models.{IOSong, Song}
import org.scalatest.{Args, BeforeAndAfterAll, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{BodyWritable, WSClient, WSResponse}
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}
import common.rich.RichFuture._
import common.rich.collections.RichSet._
import common.test.AuxSpecs

trait ControllerSpec extends AuxSpecs with GuiceOneServerPerSuite with BeforeAndAfterAll { self: TestSuite =>
  override protected def beforeAll(): Unit = {
    Module.defaultLogLevel = None
  }
  // Play, being moronic as usual, will initialize an application even if the test is to be excluded,
  // resulting in poor performance.
  // TODO create new issue in github https://github.com/playframework/playframework/issues/new
  abstract override def run(testName: Option[String], args: Args) =
    if (
      args.filter.tagsToExclude.intersects(
        getClass.getAnnotations.map(_.annotationType().getName).toSet,
      )
    )
      org.scalatest.SucceededStatus
    else
      super.run(testName, args)
  protected def applicationBuilder(additionalModules: inject.Module*): GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .overrides(TestModuleConfiguration().module)
      .overrides(additionalModules)
  protected def createApplication(additionalModules: inject.Module*): api.Application =
    applicationBuilder(additionalModules: _*).build()
  protected lazy val injector: play.api.inject.Injector = app.injector
  protected implicit lazy val ec: ExecutionContext = injector.instanceOf[ExecutionContext]
  private val config = ConfigFactory
    .load()
    .withValue("akka.loglevel", ConfigValueFactory.fromAnyRef("OFF"))
    .withValue("akka.stdout-loglevel", ConfigValueFactory.fromAnyRef("OFF"))
  private val am = ActorMaterializer()(ActorSystem.create("ControllerSpec-System", config))
  trait Byteable[A] {
    def toBytes(a: A): Array[Byte]
    def toBytes(fa: Future[A]): Array[Byte] = toBytes(fa.get)
  }
  object Byteable {
    implicit object ResultEv extends Byteable[Result] {
      override def toBytes(a: Result) = a.body.consumeData(am).get.toArray
    }
    implicit object WSResponseEv extends Byteable[WSResponse] {
      override def toBytes(a: WSResponse) = a.bodyAsBytes.toArray
    }
  }
  implicit class ByteableOps[A]($ : Future[A])(implicit ev: Byteable[A]) {
    def getBytes: Array[Byte] = ev.toBytes($)
    def getString: String = new String(getBytes, "UTF-8")
  }
  lazy val song: Song = IOSong.read(getResourceFile("/models/song.mp3"))
  lazy val encodedSong: String = PlayUrlPathUtils.encodePath(song)
  def get(path: String): Future[WSResponse] =
    app.injector.instanceOf[WSClient].url(s"http://localhost:$port/$path").get()
  def post(path: String): Future[WSResponse] = post(path, "")
  def post[B: BodyWritable](path: String, body: B = ""): Future[WSResponse] =
    app.injector.instanceOf[WSClient].url(s"http://localhost:$port/$path").post(body)

  override def fakeApplication() =
    GuiceApplicationBuilder()
      .overrides(TestModuleConfiguration().module)
      .build
}
