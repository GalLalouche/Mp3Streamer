package mains.cover

import common.io.{DirectoryRef, FileRef}
import scala.concurrent.{ExecutionContext, Future}

import scalaz.StreamT
import common.rich.func.RichStreamT

import common.concurrency.{FutureIterant, Iterant}
import common.rich.func.BetterFutureInstances._

private object LocalImageFetcher {
  private val Extensions = Set("jpg", "png")
  private def isImage(f: FileRef) = Extensions(f.extension.toLowerCase)
  def apply(dir: DirectoryRef)(implicit ec: ExecutionContext): FutureIterant[ImageSource] =
    Iterant.fromStream(StreamT.fromStream(Future {
      dir.deepFiles.toStream filter isImage map LocalSource
    }))
}
