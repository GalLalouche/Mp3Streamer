package controllers

import org.scalatest.FreeSpec
import org.scalatest.tags.Slow
import play.api.test._

import common.rich.path.RichFile._

@Slow
class PostersTest extends FreeSpec with ControllerSpec {
  private lazy val $ = app.injector.instanceOf[Posters]

  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = $.image(file.getCanonicalPath).apply(FakeRequest())
    result.getBytes shouldReturn file.bytes
  }
}
