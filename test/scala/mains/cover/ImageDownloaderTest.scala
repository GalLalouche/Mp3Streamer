package mains.cover

import java.nio.charset.MalformedInputException

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.io.MemoryRoot
import common.rich.RichFuture._
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}

import scala.concurrent.Future

class ImageDownloaderTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest with AuxSpecs {
  private val tempDir = new MemoryRoot
  private val downloader = mock[Downloader]
  private implicit val c = new TestConfiguration
  "ImageDownloader" - {
    def createDownloaderThatOnlyWorksFor(encoding: String) =
      new Downloader() {
        override def download(url: String, encoding: String) = {
          encoding match {
            case "UTF-8" => Future.successful("foobar".getBytes("UTF-8"))
            case _ => Future.failed(new MalformedInputException(0))
          }
        }
      }
    "try with several different encodings" - {
      "like UTF-8" in {
        val $ = new ImageDownloader(tempDir, createDownloaderThatOnlyWorksFor("UTF-8"))
        $("url").get.file.bytes shouldReturn "foobar".getBytes
      }
      "like UTF-16" in {
        val $ = new ImageDownloader(tempDir, createDownloaderThatOnlyWorksFor("UTF-16"))
        $("url").get.file.bytes shouldReturn "foobar".getBytes
      }
    }
    "Return none if it doesn't succeed" in {
      when(downloader.download(Matchers.anyString(), Matchers.anyString()))
          .thenReturn(Future.failed(new MalformedInputException(0)))
      val $ = new ImageDownloader(tempDir, downloader)
      $("url").value.get shouldBe 'failure
    }
  }
}
