package backend.external.expansions

import java.net.{HttpURLConnection, URL}
import java.util.regex.Pattern

import backend.Url
import backend.external.ExternalLink
import backend.recon.Reconcilable
import common.io.InternetTalker
import common.rich.RichFuture._
import common.rich.primitives.RichString._

import scala.concurrent.{ExecutionContext, Future}

private class AllMusicHelper(implicit ec: ExecutionContext, it: InternetTalker) {
  private val canonicalLink = Pattern compile "[a-zA-Z\\-0-9]+-mw\\d+"
  private val allmusicPrefx = "(?:http://www.)?allmusic.com/album/"
  private val canonicalRe = s"$allmusicPrefx($canonicalLink)".r
  private val nonCanonicalRe = s"$allmusicPrefx(.*r\\d+)".r
  //  def canonize(e: ExternalLink[Album]): Future[ExternalLink[Album]] = {
  def hasStaffReview(albumLink: String): Future[Boolean] = ???
  def hasRating(albumLink: String): Future[Boolean] = ???
  def isValidLink(albumLink: String): Future[Boolean] = ???
  def isCanonical(link: String): Boolean = canonicalRe.findAllMatchIn(link).hasNext
  def canonize[R <: Reconcilable](e: ExternalLink[R]): Future[ExternalLink[R]] = {
    def aux(url: Url): Future[Url] =
      if (canonicalLink.matcher(url.address dropAfterLast '/').matches)
        Future successful url
      else {
        val http = new URL(url.address).openConnection.asInstanceOf[HttpURLConnection]
        http.setInstanceFollowRedirects(false)
        it.connect(http)
            .filterWithMessage(_.getResponseCode == HttpURLConnection.HTTP_MOVED_PERM,
              e => s"Expected response code ${HttpURLConnection.HTTP_MOVED_PERM}, but was ${e.getResponseCode}")
            .map(_ getHeaderField "location")
            .map(Url)
      }
    aux(e.link).map(x => ExternalLink[R](x, e.host))
  }
}
