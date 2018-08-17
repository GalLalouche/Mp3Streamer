package backend.pkg

import common.io.{DirectoryRef, FileRef, RootDirectoryProvider}
import common.rich.primitives.RichBoolean._
import models.{MusicFinderProvider, Song}
import play.api.libs.json.{JsString, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

private class Zipper(songRemotePathEncoder: Song => String)(
    implicit ec: ExecutionContext, rtd: RootDirectoryProvider, mfp: MusicFinderProvider) {
  import Zipper._

  private val zipsDir = rtd.rootDirectory.addSubDir("zips")
  private val jsonCreator = createRemotePathJson(songRemotePathEncoder) _
  // TODO delete zips once in a while

  def zip(dir: DirectoryRef, overwrite: Boolean = false): Future[FileRef] = Future {
    val outputName = dir.name + ".zip"
    val existingFile = zipsDir.getFile(outputName)
    if (overwrite.isFalse && existingFile.isDefined)
      existingFile.get
    else {
      existingFile.foreach(_.delete)
      try {
        jsonCreator(dir)
        Seq(ZipAppPath, "a", "-r", "-mx0", s"${zipsDir.path}/$outputName", dir.path + "/*").!!
        zipsDir.getFile(outputName).get
      } finally {
        deleteJson(dir)
      }
    }
  }
}

private object Zipper {
  private val ZipAppPath = """c:\Program Files\7-Zip\7z.exe"""
  private[this] val JsonFileName = "remote_paths.json"

  private def createRemotePathJson(songRemotePathEncoder: Song => String)(dir: DirectoryRef)(
      implicit mfp: MusicFinderProvider): Unit = {
    val json = mfp.mf.getSongsInDir(dir).map(e => e.file.name -> JsString(songRemotePathEncoder(e)))
        .foldLeft(Json.obj())((json, next) => json + next)
    dir.addFile(JsonFileName).write(json.toString)
  }
  private def deleteJson(dir: DirectoryRef): Unit = dir.getFile(JsonFileName).map(_.delete.ensuring(x => x))
}
