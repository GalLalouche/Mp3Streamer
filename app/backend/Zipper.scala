package backend

import common.io.{DirectoryRef, FileRef, RootDirectoryProvider}
import common.rich.primitives.RichBoolean._

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

class Zipper(implicit ec: ExecutionContext, rootDirectoryProvider: RootDirectoryProvider) {
  private val zipsDir = rootDirectoryProvider.rootDirectory.addSubDir("zips")

  def zip(dir: DirectoryRef, overwrite: Boolean = false): Future[FileRef] = Future {
    val outputName = dir.name + ".zip"
    val existingFile = zipsDir.getFile(outputName)
    if (overwrite.isFalse && existingFile.isDefined)
      existingFile.get
    else {
      existingFile.foreach(_.delete)
      Seq(Zipper.ZipAppPath, "a", "-r", "-mx0", s"${zipsDir.path}/$outputName", dir.path + "/*").!!
      zipsDir.getFile(outputName).get
    }
  }
}

object Zipper {
  private val ZipAppPath = """c:\Program Files\7-Zip\7z.exe"""
}
