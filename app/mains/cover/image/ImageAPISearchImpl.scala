package mains.cover.image

import javax.inject.Inject
import mains.cover.UrlSource

import scala.concurrent.{ExecutionContext, Future}

private class ImageAPISearchImpl @Inject()(
    ec: ExecutionContext,
    fetcher: ImageAPIFetcher,
) extends ImageAPISearch {
  private implicit val iec: ExecutionContext = ec
  override def apply(terms: String): Iterator[Future[UrlSource]] =
    FutureCacher(Iterator.from(0).map(fetcher(terms, _).map(Parser.apply)))
}
