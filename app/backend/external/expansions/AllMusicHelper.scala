package backend.external.expansions

import java.net.HttpURLConnection
import java.util.regex.Pattern

import backend.Url
import backend.external.BaseLink
import backend.recon.Reconcilable
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.collections.RichTraversableOnce._
import common.rich.primitives.RichString._

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scalaz.std.TupleInstances
import scalaz.syntax.ToFoldableOps

private class AllMusicHelper(implicit it: InternetTalker) extends ToFoldableOps with TupleInstances {
  private val canonicalLink = Pattern compile "[a-zA-Z\\-0-9]+-mw\\d+"
  private val allmusicPrefx = "(?:http://www.)?allmusic.com/album/"
  private val canonicalRe = s"$allmusicPrefx($canonicalLink)".r

  def hasStaffReview(u: Url): Future[Boolean] = it.downloadDocument(u)
      .map(_.select("div[itemprop=reviewBody]").toTraversable.headOption.exists(_.html.nonEmpty))
  def hasRating(u: Url): Future[Boolean] = it.downloadDocument(u)
      .map(_.select(".allmusic-rating").toTraversable.single)
      .map(_.hasClass("rating-allmusic-0"))
      .map(!_)
  // TODO this should only be invoked once, from the external pipe
  def isValidLink(u: Url): Future[Boolean] = hasRating(u) zip hasStaffReview(u) map (_.all(identity))
  def isCanonical(link: String): Boolean = canonicalRe.findAllMatchIn(link).hasNext
  def canonize[R <: Reconcilable](e: BaseLink[R]): Future[BaseLink[R]] = {
    def aux(url: Url): Future[Url] =
      if (canonicalLink.matcher(url.address dropAfterLast '/').matches)
        Future successful url
      else {
        it.config(_ setInstanceFollowRedirects false)
            .connect(url)
            .filterWithMessage(_.getResponseCode == HttpURLConnection.HTTP_MOVED_PERM,
              e => s"Expected response code ${HttpURLConnection.HTTP_MOVED_PERM}, but was ${e.getResponseCode}")
            .map(_ getHeaderField "location")
            .map(Url)
      }
    aux(e.link).map(x => BaseLink[R](x, e.host))
  }
}
