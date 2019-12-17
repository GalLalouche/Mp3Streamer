package mains.cover

import backend.Url
import backend.module.TestModuleConfiguration
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import common.AuxSpecs
import common.io.MemoryRoot
import common.rich.RichT._

class ImageDownloaderTest extends AsyncFreeSpec with AuxSpecs {
  private val tempDir = new MemoryRoot
  private val injector = TestModuleConfiguration(_urlToBytesMapper = "foobar".getBytes.partialConst).injector
  private val $ = injector.instance[ImageDownloader].withOutput(tempDir)

  "Remote" in {
    $(UrlSource(Url("http://foobar"), 500, 500)).map {fi =>
      fi.file.bytes shouldReturn "foobar".getBytes
      fi.file.parent shouldBe tempDir
      fi.isLocal shouldReturn false
    }
  }
  "Local" in {
    val file = tempDir addFile "foo"
    $(LocalSource(file)).map {fi =>
      fi.file shouldReturn file
      fi.isLocal shouldReturn true
    }
  }
}
