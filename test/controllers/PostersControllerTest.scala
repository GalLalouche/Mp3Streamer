package controllers

import org.scalatest.FreeSpec
import org.scalatest.tags.Slow

import common.rich.path.RichFile._

@Slow
class PostersControllerTest extends FreeSpec with ControllerSpec {
  private lazy val $ = app.injector.instanceOf[PostersController]

  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = get("posters/" + file.getCanonicalPath)
    result.getBytes shouldReturn file.bytes
  }
}
