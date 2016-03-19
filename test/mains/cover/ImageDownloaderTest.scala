package mains.cover

import java.nio.charset.MalformedInputException

import common.concurrency.Impatient
import common.rich.path.RichFile._
import common.rich.path.TempDirectory
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ShouldMatchers, FreeSpec, OneInstancePerTest}

import scala.concurrent.duration._

class ImageDownloaderTest extends FreeSpec with ShouldMatchers with MockitoSugar with OneInstancePerTest {
  private val tempDir = TempDirectory()
  private val downloader = mock[Downloader]
  "ImageDownloader" - {
    def createDownloaderThatOnlyWorksFor(encoding: String) =
      new Downloader() {
        override def download(url: String, encoding: String) = {
          encoding match {
            case "UTF-8" => "foobar".getBytes
            case _ => throw new MalformedInputException(0)
          }
        }
      }
    "try with several different encodings" - {
      "like UTF-8" in {
        val $ = new ImageDownloader(tempDir, createDownloaderThatOnlyWorksFor("UTF-8"))
        $.download("url").get.file.bytes should be === "foobar".getBytes
      }
      "like UTF-16" in {
        val $ = new ImageDownloader(tempDir, createDownloaderThatOnlyWorksFor("UTF-16"))
        $.download("url").get.file.bytes should be === "foobar".getBytes
      }
    }
    "Return none if it doesn't succeed" in {
      when(downloader.download(Matchers.anyString(), Matchers.anyString()))
        .thenThrow(classOf[MalformedInputException])
      val $ = new ImageDownloader(tempDir, downloader)
      $.download("url") should be === None
    }
    "Return none after timeout" in {
      when(downloader.download(Matchers.anyString(), Matchers.anyString()))
        .thenAnswer(
          new Answer[Array[Byte]] {
            override def answer(invocation: InvocationOnMock): Array[Byte] = {
              while (true) {}
              throw new AssertionError("Wat")
            }
          }
        )
      new Impatient[Option[FolderImage]](15 millis).apply {
        new ImageDownloader(tempDir, downloader, 10 millis) download "url"
      }.get should be === None
    }
  }
}
