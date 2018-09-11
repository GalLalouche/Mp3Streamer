package common.io

import java.time.LocalDateTime

import backend.RichTime.OrderingLocalDateTime._
import common.concurrency.Extra

import scala.concurrent.Future

class FolderCleaner(dir: DirectoryRef) extends Extra {
  private val extra = Extra(s"FolderCleaner for <$dir>", {
    val minimumCreationTime = LocalDateTime.now.minusWeeks(1)
    dir.files.filter(_.lastAccessTime < minimumCreationTime).foreach(_.delete)
  })
  override def !(m: => Unit): Future[Unit] = extra.!()
}
