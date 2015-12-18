package mains.cover

import org.junit.runner.RunWith
import org.scalatest.FreeSpec
import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.OneInstancePerTest
import org.scalatest.junit.JUnitRunner
import java.util.concurrent.Semaphore
import com.google.common.base.Stopwatch
import common.rich.path.TempDirectory
import org.mockito.Mockito
import org.mockito.Matchers
import scala.io.Source
import java.io.ByteArrayInputStream
import common.rich.primitives.RichString._
import java.nio.charset.MalformedInputException
import scala.io.BufferedSource
import common.rich.path.RichFile._
import org.mockito.stubbing.Answer

@RunWith(classOf[JUnitRunner])
class ImageDownloaderTest extends FreeSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
	val tempDir = TempDirectory()
	private val downloader = mock[Downloader]
	private val $ = new ImageDownloader(tempDir, downloader)
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
			Mockito.when(downloader.download(Matchers.anyString(), Matchers.anyString()))
				.thenThrow(classOf[MalformedInputException])
			val $ = new ImageDownloader(tempDir, downloader)
			$.download("url") should be === None
		}
	}

}