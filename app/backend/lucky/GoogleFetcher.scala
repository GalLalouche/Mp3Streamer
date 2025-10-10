package backend.lucky

import com.google.inject.Inject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

import cats.data.OptionT
import common.rich.func.kats.ToMoreFunctorOps.toMoreFunctorOps

import common.io.InternetTalker
import common.io.google.GoogleSearch
import common.json.RichJson._
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce

/**
 * I don't actually use this anymore, since the API returned garbage results (the first result was
 * different from the top level Google result), so I'm keeping it here for reference.
 */
private class GoogleFetcher @Inject() (
    it: InternetTalker,
    googleSearch: GoogleSearch,
    ec: ExecutionContext,
) {
  def search(query: String): Future[String] = googleSearch(query, resultsPerQuery = 7).map(
    _.array("items").log(_.value.mkString("\n")).apply(0).str("link"),
  )

  private implicit val iec: ExecutionContext = ec

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
  private def extract(links: Iterable[Element]): Option[String] =
    links.optFilter(_.size == 1).map(_.single.attr("href"))
}
