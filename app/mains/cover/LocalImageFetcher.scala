package mains.cover

import common.io.{DirectoryRef, FileRef}

import scala.concurrent.{ExecutionContext, Future}

private object LocalImageFetcher {
  private val Extensions = Set("jpg", "png")
  private def isImage(f: FileRef) = Extensions(f.extension.toLowerCase)
  def apply(dir: DirectoryRef)(implicit ec: ExecutionContext): Future[Seq[LocalSource]] = Future {
    dir.deepFiles filter isImage map LocalSource
  }
}
