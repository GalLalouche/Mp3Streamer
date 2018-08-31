package mains.cover

import backend.Url
import backend.configs.{Configuration, TestConfiguration}
import common.AuxSpecs
import common.io.MemoryRoot
import common.rich.RichFuture._
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.{FreeSpec, OneInstancePerTest}

import scala.concurrent.ExecutionContext

class ImageDownloaderTest extends FreeSpec with AuxSpecs with OneInstancePerTest {
  private val tempDir = new MemoryRoot
  private implicit val c: Configuration = TestConfiguration(_urlToBytesMapper = "foobar".getBytes.partialConst)
  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val $ = c.injector.instance[ImageDownloader].withOutput(tempDir)

  "Remote" in {
    val fi = $(UrlSource(Url("http://foobar"), 500, 500)).get
    fi.file.bytes shouldReturn "foobar".getBytes
    fi.file.parent shouldBe tempDir
    fi.isLocal shouldReturn false
  }
  "Local" in {
    val file = tempDir addFile "foo"
    val fi = $(LocalSource(file)).get
    fi.file shouldReturn file
    fi.isLocal shouldReturn true
  }
}
