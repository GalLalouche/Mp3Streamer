package common.io

import java.time.LocalDateTime

import backend.RichTime.OrderingLocalDateTime._
import common.concurrency.Extra

class FolderCleaner(dir: DirectoryRef) extends Extra {
  override def apply(): Unit = {
    val minimumCreationTime = LocalDateTime.now.minusWeeks(1)
    dir.files.filter(_.lastModified < minimumCreationTime).foreach(_.delete)
  }
}
