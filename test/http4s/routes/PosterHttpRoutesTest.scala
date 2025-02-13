package http4s.routes

import java.io.File

import com.google.inject
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.FreeSpec
import org.scalatest.tags.Slow

import common.guice.RichModule.richModule
import common.io.{BaseDirectory, DirectoryRef, IODirectory}
import common.rich.path.RichFile.richFile

@Slow
private class PosterHttpRoutesTest extends FreeSpec with Http4sSpecs {
  protected override lazy val module: inject.Module = super.module.overrideWith(new ScalaModule {
    override def configure(): Unit = bind[DirectoryRef]
      .annotatedWith[BaseDirectory]
      .toInstance(IODirectory(new File(".").getCanonicalPath))
  })
  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = getBytes("posters/" + encoder(file.getAbsolutePath))
    result shouldReturn file.bytes
  }
}
