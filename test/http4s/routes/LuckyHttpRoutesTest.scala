package http4s.routes

import backend.module.FakeWSResponse
import http4s.routes.Http4sUtils.encode
import http4s.routes.LuckyHttpRoutesTest.UrlResult
import org.http4s.Status
import org.http4s.headers.Location
import org.scalatest.FreeSpec
import org.scalatest.tags.Slow

import common.RichUrl
import common.rich.path.RichFile.richFile

@Slow
private class LuckyHttpRoutesTest extends FreeSpec with Http4sSpecs {
  protected override lazy val baseTestModule = super.baseTestModule.copy(_urlToResponseMapper = {
    case RichUrl.Unapplied("https://duckduckgo.com/?q=%5Cfoo+bar") =>
      FakeWSResponse(
        status = Status.Ok.code,
        bytes = getResourceFile("duckduckgo_lucky_response.html").bytes,
      )
  })

  "search" in {
    get[String](s"lucky/search/${encode("foo bar")}") shouldReturn UrlResult
  }

  "redirect" in {
    val result = getRaw(s"lucky/redirect/${encode("foo bar")}")
    result.status shouldReturn Status.SeeOther
    result.headers.get[Location].get.uri.renderString shouldReturn UrlResult
  }
}

private object LuckyHttpRoutesTest {
  private val UrlResult = "https://en.wikipedia.org/wiki/Foobar"
}
