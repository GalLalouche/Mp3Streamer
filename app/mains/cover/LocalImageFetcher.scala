package mains.cover

import scala.concurrent.{ExecutionContext, Future}

import common.rich.func.BetterFutureInstances._
import scalaz.StreamT

import common.concurrency.{FutureIterant, Iterant}
import common.io.{DirectoryRef, FileRef}

private object LocalImageFetcher {
  private val Extensions = Set("jpg", "png")
  private def isImage(f: FileRef) = Extensions(f.extension.toLowerCase)
  def apply(dir: DirectoryRef)(implicit ec: ExecutionContext): FutureIterant[ImageSource] =
    Iterant.fromStreamT(StreamT.fromStream(Future {
      dir.deepFiles.toStream.filter(isImage).map(LocalSource)
    }))
}
