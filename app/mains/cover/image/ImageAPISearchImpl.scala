package mains.cover.image

import javax.inject.Inject
import mains.cover.ImageSource

import scala.concurrent.{ExecutionContext, Future}

import scalaz.StreamT
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichStreamT

import common.concurrency.{FutureIterant, Iterant}
import common.rich.RichT._

private class ImageAPISearchImpl @Inject() (
    ec: ExecutionContext,
    fetcher: ImageAPIFetcher,
) extends ImageAPISearch {
  private implicit val iec: ExecutionContext = ec
  override def apply(terms: String): FutureIterant[ImageSource] =
    RichStreamT
      .fromStream(Stream.iterate(0)(_ + 1))
      .flatMap(i => StreamT.fromStream(fetcher(terms, i).map(Parser.apply(_).toStream)))
      .|>(Iterant.fromStream[Future, ImageSource])
}
