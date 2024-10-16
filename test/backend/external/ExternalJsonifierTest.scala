package backend.external

import java.time.{LocalDate, Month}

import backend.external.extensions.{ExtendedLink, SearchExtension}
import backend.recon.{Album, Artist}
import io.lemonlabs.uri.Url
import org.scalatest.AsyncFreeSpec
import play.api.libs.json.Json

import scala.concurrent.Future

import common.test.AuxSpecs

class ExternalJsonifierTest extends AsyncFreeSpec with AuxSpecs {
  private val $ = new ExternalJsonifier()
  private val time = LocalDate.of(1980, Month.OCTOBER, 17).atStartOfDay

  "toJsonOrError" - {
    "new wikipedia links are unmarked" in {
      val links = Future.successful(
        TimestampedExtendedLinks[Album](
          Vector(
            new ExtendedLink(
              Url.parse("https://www.wikidata.org/wiki/Q1340975"),
              Host.Wikidata,
              LinkMark.Text("The River (1980 double studio album by Bruce Springsteen)"),
              extensions = Nil,
            ),
            new ExtendedLink(
              Url.parse("https://en.wikipedia.org/wiki/The_River_New(Bruce_Springsteen_album)"),
              Host.Wikipedia,
              LinkMark.New,
              extensions = Nil,
            ),
            SearchExtension(Host.AllMusic, Album("The River", 1980, Artist("Bruce Springsteen"))),
          ),
          time,
        ),
      )
      $.toJsonOrError(links).map {
        _ shouldReturn Json.obj(
          "links" -> Json.obj(
            "Wikipedia" -> Json.obj(
              "host" -> "Wikipedia",
              "main" -> "https://en.wikipedia.org/wiki/The_River_New(Bruce_Springsteen_album)",
              "extensions" -> Json.obj(),
            ),
            "Wikidata" -> Json.obj(
              "host" -> "Wikidata* The River (1980 double studio album by Bruce Springsteen)",
              "main" -> "https://www.wikidata.org/wiki/Q1340975",
              "extensions" -> Json.obj(),
            ),
            "AllMusic" -> Json.obj(
              "host" -> "AllMusic?",
              "main" -> "javascript:void(0)",
              "extensions" -> Json.obj(
                "Google" -> "https://www.google.com/search?q=AllMusic+bruce+springsteen+-+the+river",
                "Lucky" -> "lucky/redirect/AllMusic%20bruce%20springsteen%20-%20the%20river",
              ),
            ),
          ),
          "timestamp" -> "17/10",
        )
      }
    }
    "missing wikipedia links are marked" in {
      val links = Future.successful(
        TimestampedExtendedLinks[Album](
          Vector(
            SearchExtension(Host.Wikipedia, Album("The River", 1980, Artist("Bruce Springsteen"))),
          ),
          time,
        ),
      )
      $.toJsonOrError(links).map {
        _ shouldReturn Json.obj(
          "links" -> Json.obj(
            "Wikipedia" -> Json.obj(
              "host" -> "Wikipedia?",
              "main" -> "javascript:void(0)",
              "extensions" -> Json.obj(
                "Google" -> "https://www.google.com/search?q=Wikipedia+bruce+springsteen+-+the+river",
                "Lucky" -> "lucky/redirect/Wikipedia%20bruce%20springsteen%20-%20the%20river",
              ),
            ),
          ),
          "timestamp" -> "17/10",
        )
      }
    }
    "error" in {
      $.toJsonOrError(Future.failed(new Exception("foobar"))).map {
        _ shouldReturn Json.obj("error" -> "foobar")
      }
    }
  }
}
