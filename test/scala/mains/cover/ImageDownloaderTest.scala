package mains.cover

import java.nio.charset.MalformedInputException

import backend.configs.TestConfiguration
import common.AuxSpecs
import common.concurrency.Impatient
import common.io.MemoryRoot
import common.rich.RichFuture._
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest, ShouldMatchers}

import scala.concurrent.Future
import scala.concurrent.duration._

class ImageDownloaderTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest with AuxSpecs {
  private val tempDir = new MemoryRoot
  private val downloader = mock[Downloader]
  private implicit val c = TestConfiguration
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
    "Return none after timeout" in {
      when(downloader.download(Matchers.anyString(), Matchers.anyString())).thenAnswer(
        new Answer[Array[Byte]] {
          override def answer(invocation: InvocationOnMock): Array[Byte] = {
            while (true) {}
            throw new AssertionError("Wat")
          }
        }
      )
      new Impatient[Int](15 millis).apply {
        Thread sleep 50
        1
      } shouldBe 'empty
    }
  }
}
