package mains.cover

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{FutureIterant, Iterant}
import common.io.{DirectoryRef, FileRef}

private object LocalImageFetcher {
  private val Extensions = Set("jpg", "png")
  private def isImage(f: FileRef) = Extensions(f.extension.toLowerCase)
  def apply(dir: DirectoryRef)(implicit ec: ExecutionContext): FutureIterant[ImageSource] =
    Iterant.from[Future, ImageSource](
      Future(dir.deepFiles.to(LazyList).filter(isImage).map(LocalSource)),
    )
}
