package server

import backend.module.FakeWSResponse
import com.google.inject.Module
import org.http4s.Status
import server.LuckyTest.UrlResult
import sttp.client3.UriContext
import sttp.model.{Header, StatusCode}

import common.RichUrl
import common.rich.path.RichFile.richFile

private class LuckyTest(module: Module) extends HttpServerSpecs(module: Module) {
  protected override def baseTestModule = super.baseTestModule.copy(_urlToResponseMapper = {
    case RichUrl.Unapplied("https://duckduckgo.com/?q=%5Cfoo+bar") =>
      FakeWSResponse(
        status = Status.Ok.code,
        bytes = getResourceFile("duckduckgo_lucky_response.html").bytes,
      )
  })

  "search" in {
    getString(uri"lucky/search/${"foo bar"}") shouldEventuallyReturn UrlResult
  }

  "redirect" in {
    getRaw(uri"lucky/redirect/${"foo bar"}").map { r =>
      r.code shouldReturn StatusCode.SeeOther
      r.headers shouldContain Header("location", UrlResult)
    }
  }
}

private object LuckyTest {
  private val UrlResult = "https://en.wikipedia.org/wiki/Foobar"
}
