package backend.external.expansions

import java.net.HttpURLConnection
import java.util.regex.Pattern

import backend.Url
import backend.external.BaseLink
import backend.recon.Reconcilable
import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.collections.RichTraversableOnce._
import common.rich.func.ToMoreMonadErrorOps
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scalaz.std.{FutureInstances, TupleInstances}
import scalaz.syntax.ToFoldableOps

private class AllMusicHelper(implicit it: InternetTalker) extends ToFoldableOps with TupleInstances
    with FutureInstances with ToMoreMonadErrorOps {
  private val canonicalLink = Pattern compile "[a-zA-Z\\-0-9]+-mw\\d+"
  private val allmusicPrefix = "(?:http://www.)?allmusic.com/album/"
  private val canonicalRe = s"$allmusicPrefix($canonicalLink)".r

  @VisibleForTesting
  def hasStaffReview(u: Url): Future[Boolean] = it.downloadDocument(u)
      .map(_.select("div[itemprop=reviewBody]").asScala.headOption.exists(_.html.nonEmpty))
  @VisibleForTesting
  def hasRating(u: Url): Future[Boolean] = it.downloadDocument(u)
      .map(_.select(".allmusic-rating").asScala.single.hasClass("rating-allmusic-0").isFalse)
  // TODO this should only be invoked once, from the external pipe
  def isValidLink(u: Url): Future[Boolean] = for {
    rated <- hasRating(u)
    staffReviewed <- hasStaffReview(u)
  } yield rated && staffReviewed
  def isCanonical(link: String): Boolean = canonicalRe.findAllMatchIn(link).hasNext

  def canonize[R <: Reconcilable](link: BaseLink[R]): Future[BaseLink[R]] = {
    val MaxTries = 5
    def followRedirect(currentTry: Int)(url: Url): Future[Url] =
      if (canonicalLink.matcher(url.address dropAfterLast '/').matches)
        Future successful url
      else if (currentTry >= MaxTries) {
        it.logger.warn(s"AllMusic canonization gave up after <$MaxTries> tries")
        Future successful url
      } else
        it.useWs(_.url(url.address).withFollowRedirects(false).get())
            .filterWithMessageF(_.status == HttpURLConnection.HTTP_MOVED_PERM,
              e => s"Expected response code HTTP_MOVED_PERM (${HttpURLConnection.HTTP_MOVED_PERM}), " +
                  s"but was ${e.statusText} (${e.status})")
            .map(_.header("location").get)
            .map(Url)
            .flatMap(followRedirect(currentTry + 1))
    followRedirect(0)(link.link).map(BaseLink[R](_, link.host))
  }
}
