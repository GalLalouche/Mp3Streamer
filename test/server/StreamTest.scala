package server

import java.io.File
import java.net.URI

import com.google.inject.Module
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.BeforeAndAfterEach
import sttp.client3.UriContext

import common.io.BaseDirectory
import common.path.ref.DirectoryRef
import common.path.ref.io.IODirectory
import common.rich.RichFile.richFile

private class StreamTest(serverModule: Module)
    extends HttpServerSpecs(serverModule)
    with BeforeAndAfterEach {
  protected override lazy val overridingModule = new ScalaModule {
    override def configure(): Unit = bind[DirectoryRef]
      .annotatedWith[BaseDirectory]
      .toInstance(IODirectory(getClass.getResource("/").getPath))
  }
  "song" in {
    val file = getResourceFile("/models/song.mp3")
    val relativePath = new File(".").getCanonicalFile.toPath.relativize(file.toPath).toString
    getBytes(uri"/stream/download/$relativePath") shouldEventuallyReturn file.bytes
  }
}

