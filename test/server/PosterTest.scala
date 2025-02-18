package server

import java.io.File

import net.codingwell.scalaguice.ScalaModule
import org.scalatest.tags.Slow
import sttp.client3.UriContext

import common.io.{BaseDirectory, DirectoryRef, IODirectory}
import common.rich.path.RichFile.richFile

@Slow
private class PosterTest extends Http4sEndToEndSpecs {
  protected override lazy val overridingModule = new ScalaModule {
    override def configure(): Unit = bind[DirectoryRef]
      .annotatedWith[BaseDirectory]
      .toInstance(IODirectory(new File(".").getCanonicalPath))
  }
  "image" in {
    val file = getResourceFile("poster.jpg")
    val result = getBytes(uri"/posters/$file")
    result shouldEventuallyReturn file.bytes
  }
}
