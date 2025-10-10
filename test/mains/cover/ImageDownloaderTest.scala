package mains.cover

import backend.module.TestModuleConfiguration
import common.io.MemoryRoot
import common.rich.RichT._
import common.test.AuxSpecs
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

class ImageDownloaderTest extends AsyncFreeSpec with AuxSpecs {
  private val tempDir = new MemoryRoot
  private val injector =
    TestModuleConfiguration(_urlToBytesMapper = "foobar".getBytes.partialConst).injector
  private val $ = injector.instance[ImageDownloader].withOutput(tempDir)

  "Remote" in {
    $(UrlSource(Url.parse("http://foobar"), 500, 500)).map { fi =>
      fi.file.bytes shouldReturn "foobar".getBytes
      fi.file.parent shouldBe tempDir
      fi.isLocal shouldReturn false
    }
  }
  "Local" in {
    val file = tempDir.addFile("foo")
    $(LocalSource(file)).map { fi =>
      fi.file shouldReturn file
      fi.isLocal shouldReturn true
    }
  }
}
