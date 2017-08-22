package mains.cover
import common.io.{DirectoryRef, FileRef}

import scala.concurrent.{ExecutionContext, Future}

private object LocalImageFetcher {
  private val extensions = Set("jpg", "png")
  private def isImage(f: FileRef) = extensions(f.extension.toLowerCase)
  def apply(dir: DirectoryRef)(implicit c: ExecutionContext): Future[Seq[LocalSource]] = Future {
    dir.deepFiles filter isImage map LocalSource
  }
}
