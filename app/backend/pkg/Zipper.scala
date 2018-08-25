package backend.pkg

import backend.configs.Configuration
import common.io.{DirectoryRef, FileRef, RootDirectory}
import common.rich.primitives.RichBoolean._
import models.{MusicFinder, Song}
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.libs.json.{Json, JsString}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

private class Zipper(songRemotePathEncoder: Song => String)(implicit c: Configuration) {
  import Zipper._

  private implicit val ec: ExecutionContext = c.injector.instance[ExecutionContext]
  private val rootDirectory = c.injector.instance[DirectoryRef, RootDirectory]
  private val zipsDir = rootDirectory.addSubDir("zips")
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
      implicit c: Configuration): Unit = {
    val mf = c.injector.instance[MusicFinder]
    val json = mf.getSongsInDir(dir).map(e => e.file.name -> JsString(songRemotePathEncoder(e)))
        .foldLeft(Json.obj())((json, next) => json + next)
    dir.addFile(JsonFileName).write(json.toString)
  }
  private def deleteJson(dir: DirectoryRef): Unit = dir.getFile(JsonFileName).map(_.delete.ensuring(x => x))
}
