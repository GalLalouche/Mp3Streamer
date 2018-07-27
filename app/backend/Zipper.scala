package backend

import common.io.{DirectoryRef, FileRef, RootDirectoryProvider}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

class Zipper(implicit ec: ExecutionContext, rootDirectoryProvider: RootDirectoryProvider) {
  private val zipsDir = rootDirectoryProvider.rootDirectory.addSubDir("zips")

  def zip(dir: DirectoryRef): Future[FileRef] = Future {
    val outputName = dir.name + ".zip"
    zipsDir.getFile(outputName).foreach(_.delete)
    Seq(Zipper.ZipAppPath, "a", "-r", "-mx0",
      s"${zipsDir.path}/$outputName",
      dir.path + "/*").!!
    val $ = zipsDir.getFile(outputName)
    assert($.isDefined)
    $.get
  }
}

object Zipper {
  private val ZipAppPath = """c:\Program Files\7-Zip\7z.exe"""
}
