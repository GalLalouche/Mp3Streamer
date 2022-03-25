package common.io

import java.time.LocalDateTime

import scala.Ordering.Implicits._
import scala.concurrent.Future

import common.concurrency.Extra
import common.rich.RichTime.OrderingLocalDateTime

class FolderCleaner(dir: DirectoryRef) {
  private val extra = Extra(s"FolderCleaner for <$dir>") {
    val minimumCreationTime = LocalDateTime.now.minusWeeks(1)
    dir.files.filter(_.lastAccessTime < minimumCreationTime).foreach(_.delete)
  }
  def clean(): Future[Unit] = extra.!()
}
