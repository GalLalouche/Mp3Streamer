package backend.external

import java.time.{LocalDate, Month}

import backend.external.extensions.{ExtendedLink, SearchExtension}
import backend.Url
import backend.module.TestModuleConfiguration
import backend.recon.{Album, Artist}
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

import common.AuxSpecs

class ExternalJsonifierTest extends FreeSpec with AuxSpecs {
  private val injector = TestModuleConfiguration().injector
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val $ = new ExternalJsonifier()
  private val time = LocalDate.of(1980, Month.OCTOBER, 17).atStartOfDay

  "toJsonOrError" - {
    "new wikipedia links are unmarked" in {
      val links = Future.successful(TimestampedExtendedLinks[Album](Vector(
        new ExtendedLink(
          Url("https://www.wikidata.org/wiki/Q1340975"),
          Host.Wikidata,
          LinkMark.New,
          extensions = Nil,
        ),
        new ExtendedLink(
          Url("https://en.wikipedia.org/wiki/The_River_New(Bruce_Springsteen_album)"),
          Host.Wikipedia,
          LinkMark.New,
          extensions = Nil,
        ),
        SearchExtension(Host.AllMusic, Album("The River", 1980, Artist("Bruce Springsteen"))),
      ), time))
      $.toJsonOrError(links) map {
        _ shouldReturn Json.obj(
          "Wikipedia" -> Json.obj(
            "host" -> "Wikipedia",
            "main" -> "https://en.wikipedia.org/wiki/The_River_New(Bruce_Springsteen_album)",
            "extensions" -> Json.obj(),
          ),
          "Wikidata" -> Json.obj(
            "host" -> "Wikidata*",
            "main" -> "https://www.wikidata.org/wiki/Q1340975",
            "extensions" -> Json.obj(),
          ),
          "AllMusic" -> Json.obj(
            "host" -> "AllMusic?",
            "main" -> "javascript:void(0)",
            "extensions" -> Json.obj("Google" -> "http://www.google.com/search?q=bruce springsteen - the river AllMusic"),
          ),
          "timestamp" -> "17/10",
        )
      }
    }
    "missing wikipedia links are marked" in {
      val links = Future.successful(TimestampedExtendedLinks[Album](Vector(
        SearchExtension(Host.Wikipedia, Album("The River", 1980, Artist("Bruce Springsteen"))),
      ), time))
      $.toJsonOrError(links) map {
        _ shouldReturn Json.obj(
          "Wikipedia" -> Json.obj(
            "host" -> "Wikipedia?",
            "main" -> "javascript:void(0)",
            "extensions" -> Json.obj("Google" -> "http://www.google.com/search?q=bruce springsteen - the river Wikipedia"),
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
