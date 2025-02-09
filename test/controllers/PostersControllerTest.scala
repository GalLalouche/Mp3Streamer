package controllers

import java.io.File

import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.FreeSpec
import org.scalatest.tags.Slow
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder

import common.guice.RichModule.richModule
import common.io.{BaseDirectory, DirectoryRef, IODirectory, IOFile}
import common.rich.RichFuture.richFuture
import common.rich.path.RichFile._

@Slow
class PostersControllerTest extends FreeSpec with ControllerSpec {
  override def fakeApplication() =
    GuiceApplicationBuilder()
      .overrides(
        TestModuleConfiguration().module.overrideWith(new ScalaModule {
          override def configure(): Unit =
            bind[DirectoryRef]
              .annotatedWith[BaseDirectory]
              .toInstance(IODirectory(new File(".").getCanonicalPath))
        }),
      )
      .build
  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = get("posters/" + PlayUrlEncoder(new IOFile(file))).get
    if (result.status != Status.OK)
      fail(s"Get failed!:\n" + result.body)
    result.bodyAsBytes.toArrayUnsafe() shouldReturn file.bytes
  }
}
