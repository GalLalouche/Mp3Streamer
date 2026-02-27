package server

import java.io.File

import com.google.inject.Module
import sttp.client3.UriContext

import common.rich.RichFile.richFile

private class ApplicationTest(serverModule: Module) extends HttpServerSpecs(serverModule) {
  private val expectedBytes = new File("public/html/main.html").bytes
  "GET /" in {
    getBytes(uri"/") shouldEventuallyReturn expectedBytes
  }
  "GET /mute" in {
    getBytes(uri"/mute") shouldEventuallyReturn expectedBytes
  }
  "GET /mp3" in {
    getBytes(uri"/mp3") shouldEventuallyReturn expectedBytes
  }
}
