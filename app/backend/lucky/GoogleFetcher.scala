package backend.lucky

import javax.inject.Inject
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.concurrent.{ExecutionContext, Future}
import scalaz.OptionT

import common.io.google.GoogleSearch
import common.io.InternetTalker
import common.json.RichJson._
import common.rich.collections.RichTraversableOnce.richTraversableOnce
import common.rich.func.BetterFutureInstances.betterFutureInstances
import common.rich.func.ToMoreFunctorOps.toMoreFunctorOps
import common.rich.RichT.richT
import org.jsoup.nodes.Element
import org.jsoup.Jsoup

/**
 * I don't actually use this anymore, since the API returned garbage results (the first result was
 * different than the top level Google result), so I'm keeping it here for reference.
 */
private class GoogleFetcher @Inject() (
    it: InternetTalker,
    googleSearch: GoogleSearch,
) {
  def search(query: String): Future[String] =
    googleSearch(query, resultsPerQuery = 7).map(
      _.array("items").log(_.value.mkString("\n")).apply(0).str("link"),
    )

  private implicit val iec: ExecutionContext = it

  // Manually call google.com using I'm feeling lucky syntax and see if it redirects. The API call
  // is probably smarter, but I'm keeping this as reference.
  // noinspection ScalaUnusedSymbol
  private def withoutApi(query: String): OptionT[Future, String] =
    it
      .useWs(
        _.url(s"http://www.google.com/search?ie=UTF-8&oe=UTF-8&sourceid=navclient&gfns=1&q=$query")
          .withFollowRedirects(false)
          .get(),
      )
      .toOptionTF(r => extract(Jsoup.parse(r.body).select("A").asScala))
  private def extract(links: Seq[Element]): Option[String] =
    links.optFilter(_.size == 1).map(_.single.attr("href"))
}
