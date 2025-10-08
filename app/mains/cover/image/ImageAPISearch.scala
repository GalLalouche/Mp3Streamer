package mains.cover.image

import com.google.inject.Inject
import mains.cover.ImageSource

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichStreamT
import scalaz.StreamT

import common.concurrency.{FutureIterant, Iterant}
import common.rich.RichT._

private[cover] class ImageAPISearch @Inject() (
    ec: ExecutionContext,
    fetcher: ImageAPIFetcher,
) {
  private implicit val iec: ExecutionContext = ec
  def apply(terms: String): FutureIterant[ImageSource] =
    RichStreamT
      .fromStream(Stream.iterate(0)(_ + 1))
      .flatMap(i => StreamT.fromStream(fetcher(terms, i).map(Parser.apply(_).toStream)))
      .|>(Iterant.fromStreamT[Future, ImageSource])
}
