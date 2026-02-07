package mains.cover

import scala.concurrent.{ExecutionContext, Future}

import common.concurrency.{FutureIterant, Iterant}
import common.path.ref.{DirectoryRef, FileRef}

private object LocalImageFetcher {
  private val Extensions = Vector("jpg", "png")
  private def isImage(f: FileRef) = f.extensionIsAnyOf(Extensions)
  def apply(dir: DirectoryRef)(implicit ec: ExecutionContext): FutureIterant[ImageSource] =
    Iterant.from[Future, ImageSource](
      Future(dir.deepFiles.to(LazyList).filter(isImage).map(LocalSource)),
    )
}
