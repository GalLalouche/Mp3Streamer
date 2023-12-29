package backend.lucky

import javax.inject.Inject

import backend.lucky.DuckDuckgoFetcher.{QueryPrefix, RutPrefix, UrlPrefix}
import controllers.UrlDecodeUtils
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}

import common.RichJsoup.richElement
import common.io.InternetTalker
import common.rich.RichT.richT
import common.rich.collections.RichTraversableOnce.richTraversableOnce

private class DuckDuckgoFetcher @Inject() (it: InternetTalker, decoder: UrlDecodeUtils) {
  private implicit val iec: ExecutionContext = it
  def search(query: String): Future[String] =
    it.useWs(_.url(s"$QueryPrefix$query").withFollowRedirects(false).get)
      .map(_.body |> parse)

  private def parse(s: String): String =
    Jsoup
      .parse(s)
      .selectIterator("meta")
      .flatMap(_.attrOpt("content"))
      .filter(_.startsWith(UrlPrefix))
      .single
      .drop(UrlPrefix.length)
      .mapIf(_.contains(RutPrefix))
      .to(s => s.take(s.indexOf(RutPrefix)))
      .|>(decoder.decode)
}

private object DuckDuckgoFetcher {
  private val QueryPrefix = "https://duckduckgo.com/?q=\\!"
  private val UrlPrefix = "0; url=/l/?uddg="
  private val RutPrefix = "&rut"
}
