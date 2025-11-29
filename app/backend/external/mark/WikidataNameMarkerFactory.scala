package backend.external.mark

import backend.external.{Host, LinkMark, MarkedLink}
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import com.google.inject.Inject
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

import common.RichJsoup._
import common.io.InternetTalker
import common.rich.RichT._

private class WikidataNameMarkerFactory @Inject() (it: InternetTalker, ec: ExecutionContext) {
  private implicit val iec: ExecutionContext = ec
  def create[R <: Reconcilable]: ExternalLinkMarker[R] = new ExternalLinkMarker[R] {
    override def host = Host.Wikidata
    override def apply(l: MarkedLink[R]) = WikidataNameMarkerFactory(l, it)
  }
}

private object WikidataNameMarkerFactory {
  @VisibleForTesting def extract(d: Document): String = {
    val title = d.selectSingle("#content .wikibase-title-label").text
    val description = d.selectSingle(".wikibase-entitytermsview-heading-description").text
    s"$title ($description)"
  }
  private def apply[R <: Reconcilable](
      l: MarkedLink[R],
      it: InternetTalker,
  )(implicit ec: ExecutionContext): Future[LinkMark] =
    if (l.isNew) it.downloadDocument(l.link).map(extract(_) |> LinkMark.Text.apply)
    else Future.successful(l.mark)
}
