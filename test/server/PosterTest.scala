package server

import java.io.File

import com.google.inject.Module
import net.codingwell.scalaguice.ScalaModule
import sttp.client3.UriContext

import common.io.BaseDirectory
import common.rich.RichFile.richFile
import common.path.ref.DirectoryRef
import common.path.ref.io.IODirectory

private class PosterTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
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
