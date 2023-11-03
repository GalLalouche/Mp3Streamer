package controllers

import org.scalatest.tags.Slow
import org.scalatest.FreeSpec

import common.rich.path.RichFile._
import play.api.test._

@Slow
class PostersTest extends FreeSpec with ControllerSpec {
  private val $ = app.injector.instanceOf[Posters]

  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = $.image(file.getCanonicalPath).apply(FakeRequest())
    result.getBytes shouldReturn file.bytes
  }
}
