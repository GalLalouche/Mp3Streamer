package backend.external.mark

import com.google.inject.Inject

import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import org.jsoup.nodes.Document

import scala.concurrent.Future

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.RichT._
import common.rich.primitives.RichBoolean._

private class WikidataNameMarkerFactory @Inject() (implicit it: InternetTalker) {
  def create[R <: Reconcilable]: ExternalLinkMarker[R] = new ExternalLinkMarker[R] {
    override def host = Host.Wikidata
    override def apply(l: MarkedLink[R]) = WikidataNameMarkerFactory(l)
  }
}

private object WikidataNameMarkerFactory {
  @VisibleForTesting def extract(d: Document): String = {
    val title = d.selectSingle(".wikibase-title-label").text
    val description = d.selectSingle(".wikibase-entitytermsview-heading-description").text
    s"$title ($description)"
  }
  private def apply[R <: Reconcilable](
      l: MarkedLink[R],
  )(implicit it: InternetTalker): Future[LinkMark] =
    if (l.isNew.isFalse) Future.successful(l.mark)
    else it.downloadDocument(l.link).map(extract(_) |> LinkMark.Text.apply)
}
