package common.io

import java.time.LocalDateTime

import backend.RichTime.OrderingLocalDateTime._
import common.concurrency.AbstractExtra

class FolderCleaner(dir: DirectoryRef) extends AbstractExtra {
  override def apply(): Unit = {
    val minimumCreationTime = LocalDateTime.now.minusWeeks(1)
    dir.files.filter(_.lastAccessTime < minimumCreationTime).foreach(_.delete)
  }
}
