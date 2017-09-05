package mains.cover

import backend.Url
import backend.configs.TestConfiguration
import common.AuxSpecs
import common.io.MemoryRoot
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

class ImageDownloaderTest extends FreeSpec with AuxSpecs {
  private val tempDir = new MemoryRoot
  private implicit val c = TestConfiguration(_urlToBytesMapper = "foobar".getBytes.partialConst)
  "Remote" in {
    val $ = new ImageDownloader(tempDir)
    val fi = $(UrlSource(Url("http://foobar"), 500, 500)).get
    fi.file.bytes shouldReturn "foobar".getBytes
    fi.file.parent shouldBe tempDir
    fi.isLocal shouldReturn false
  }
  "Local" in {
    val $ = new ImageDownloader(tempDir)
    val file = tempDir addFile "foo"
    val fi = $(LocalSource(file)).get
    fi.file shouldReturn file
    fi.isLocal shouldReturn true
  }
}
