package server

import java.io.File

import com.google.inject.Module
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.BeforeAndAfterEach
import sttp.client3.UriContext

import common.io.{BaseDirectory, DirectoryRef, IODirectory}
import common.rich.path.RichFile.richFile

private class StreamTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEach {
  protected override lazy val overridingModule = new ScalaModule {
    override def configure(): Unit = bind[DirectoryRef]
      .annotatedWith[BaseDirectory]
      .toInstance(IODirectory(new File(".").getCanonicalPath))
  }
  "song" in {
    val file = getResourceFile("/models/song.mp3")
    val result = getBytes(uri"/stream/download/${file.getAbsolutePath}")
    result shouldEventuallyReturn file.bytes
  }
}
