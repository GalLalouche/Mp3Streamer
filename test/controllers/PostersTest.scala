package controllers

import common.rich.path.RichFile._
import common.RichClass._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec
import play.api.test._

class PostersTest extends FreeSpec with ControllerSpec {
  private val $ = injector.instance[Posters]

  "image" in {
    val file = getClass.getFile("poster.jpg")
    val result = $.image(file.getCanonicalPath).apply(FakeRequest())
    getBytes(result) shouldReturn file.bytes
  }
}
