package backend.external.expansions

import java.net.HttpURLConnection
import java.util.regex.Pattern
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

import backend.external.expansions.AllMusicHelper._
import backend.external.BaseLink
import backend.logging.Logger
import backend.recon.Reconcilable
import backend.Url
import com.google.common.annotations.VisibleForTesting
import common.io.InternetTalker
import common.rich.func.BetterFutureInstances._
import common.rich.func.ToMoreMonadErrorOps._
import common.rich.primitives.RichBoolean._
import common.rich.primitives.RichString._
import common.RichJsoup._
import org.jsoup.nodes.Document

private class AllMusicHelper @Inject() (
    it: InternetTalker,
    logger: Logger,
) {
  private implicit val iec: ExecutionContext = it
  private val canonicalLink = Pattern.compile("[a-zA-Z\\-0-9]+-mw\\d+")
  private val allmusicPrefix = "(?:http://www.)?allmusic.com/album/"
  private val canonicalRe = s"$allmusicPrefix($canonicalLink)".r

  // TODO this should only be invoked once, from the external pipe
  def isValidLink(u: Url): Future[Boolean] =
    it.downloadDocument(u)
      .map(d => hasRating(d) && hasStaffReview(d))
  def isCanonical(link: String): Boolean = canonicalRe.findAllMatchIn(link).hasNext

  def canonize[R <: Reconcilable](link: BaseLink[R]): Future[BaseLink[R]] = {
    val MaxTries = 5
    def followRedirect(currentTry: Int)(url: Url): Future[Url] =
      if (canonicalLink.matcher(url.address.takeAfterLast('/')).matches)
        Future.successful(url)
      else if (currentTry >= MaxTries) {
        logger.warn(s"AllMusic canonization gave up after <$MaxTries> tries")
        Future.successful(url)
      } else
        it.useWs(_.url(url.address).withFollowRedirects(false).get())
          .filterWithMessageF(
            _.status == HttpURLConnection.HTTP_MOVED_PERM,
            e =>
              s"Expected response code HTTP_MOVED_PERM (${HttpURLConnection.HTTP_MOVED_PERM}), " +
                s"but was ${e.statusText} (${e.status})",
          )
          .map(_.header("location").get)
          .map(Url.apply)
          .flatMap(followRedirect(currentTry + 1))
    followRedirect(0)(link.link).map(BaseLink[R](_, link.host))
  }
}

private object AllMusicHelper {
  @VisibleForTesting
  def hasStaffReview(d: Document): Boolean =
    d.find(".review .text").exists(_.html.nonEmpty)
  @VisibleForTesting
  def hasRating(d: Document): Boolean =
    d.selectSingle(".allmusic-rating").hasClass("rating-allmusic-0").isFalse
}
