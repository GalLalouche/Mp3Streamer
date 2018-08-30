package backend.pkg

import backend.Retriever
import common.io.{DirectoryRef, FileRef, RootDirectory}
import common.rich.func.ToMoreFoldableOps
import javax.inject.Inject
import models.{MusicFinder, Song}
import play.api.libs.json.{Json, JsString}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

import scalaz.std.OptionInstances

private class ZipperFactory @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    @RootDirectory rootDirectory: DirectoryRef,
) extends ToMoreFoldableOps with OptionInstances {
  private implicit val iec: ExecutionContext = ec
  import ZipperFactory._

  def apply(songRemotePathEncoder: Song => String): Retriever[DirectoryRef, FileRef] = {
    val zipsDir = rootDirectory.addSubDir("zips")
    val jsonCreator = createRemotePathJson(songRemotePathEncoder, mf) _
    dir =>
      Future {
        // TODO delete zips once in a while
        val outputName = dir.name + ".zip"
        val existingFile = zipsDir.getFile(outputName)
        existingFile.getOrElse {
          existingFile.foreach(_.delete)
          try {
            jsonCreator(dir)
            Seq(ZipAppPath, "a", "-r", "-mx0", s"${
              zipsDir.path
            }/$outputName", dir.path + "*").!!
            zipsDir.getFile(outputName).get
          } finally {
            deleteJson(dir)
          }
        }
      }
  }
}

private object ZipperFactory {
  private val ZipAppPath = """c:\Program Files\7-Zip\7z.exe"""
  private[this] val JsonFileName = "remote_paths.json"

  private def createRemotePathJson(songRemotePathEncoder: Song => String, mf: MusicFinder)(
      dir: DirectoryRef): Unit = {
    val json = mf.getSongsInDir(dir).map(e => e.file.name -> JsString(songRemotePathEncoder(e)))
        .foldLeft(Json.obj())((json, next) => json + next)
    dir.addFile(JsonFileName).write(json.toString)
  }
  private def deleteJson(dir: DirectoryRef): Unit = dir.getFile(JsonFileName).map(_.delete.ensuring(x => x))
}
