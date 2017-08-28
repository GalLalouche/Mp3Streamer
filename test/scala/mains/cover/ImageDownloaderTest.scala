package mains.cover

import backend.Url
import backend.configs.TestConfiguration
import common.AuxSpecs
import common.io.MemoryRoot
import common.rich.RichFuture._
import common.rich.RichT._
import common.rich.primitives.RichString._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}

class ImageDownloaderTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest with AuxSpecs {
  private val tempDir = new MemoryRoot
  private implicit val c = TestConfiguration(_urlToBytesMapper = "foobar".getBytes.const)
  "Remote" in {
    val $ = new ImageDownloader(tempDir)
    val f = $(UrlSource(Url("http://foobar"))).get.file
    f.bytes shouldReturn "foobar".getBytes
    f.parent shouldBe tempDir
  }
  "Local" in {
    val $ = new ImageDownloader(tempDir)
    val file = tempDir addFile "foo"
    $(LocalSource(file)).get shouldReturn FolderImage(file, isLocal = true)
  }
}
