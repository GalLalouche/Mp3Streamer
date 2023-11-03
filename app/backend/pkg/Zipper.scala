package backend.pkg

import backend.Retriever
import controllers.UrlPathUtils
import javax.inject.Inject
import models.MusicFinder
import play.api.libs.json.{Json, JsString}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

import scalaz.syntax.apply.ToApplyOps
import common.rich.func.BetterFutureInstances._

import common.io.{DirectoryRef, FileRef, FolderCleaner, RootDirectory}

private class Zipper @Inject() (
    ec: ExecutionContext,
    mf: MusicFinder,
    urlPathUtils: UrlPathUtils,
    @RootDirectory rootDirectory: DirectoryRef,
) extends Retriever[DirectoryRef, FileRef] {
  import Zipper._

  private implicit val iec: ExecutionContext = ec
  private val zipsDir = rootDirectory.addSubDir("zips")
  private val createJsonIn = createRemotePathJson(mf, urlPathUtils) _
  private val cleaner = new FolderCleaner(zipsDir)

  override def apply(dir: DirectoryRef): Future[FileRef] = Future {
    val outputName = dir.name + ".zip"
    zipsDir
      .getFile(outputName)
      .getOrElse(
        try {
          createJsonIn(dir)
          Vector(ZipAppPath, "a", "-r", "-mx0", s"${zipsDir.path}/$outputName", dir.path + "/*").!!
          zipsDir.getFile(outputName).get
        } finally
          deleteJson(dir),
      )
  } <* cleaner.clean()
}

private object Zipper {
  private val ZipAppPath = """C:\Program Files\7-Zip\7z.exe"""
  private[this] val JsonFileName = "remote_paths.json"

  private def createRemotePathJson(mf: MusicFinder, urlPathUtils: UrlPathUtils)(
      dir: DirectoryRef,
  ): Unit = {
    val json = mf
      .getSongsInDir(dir)
      .map(e => e.file.name -> JsString(urlPathUtils.encodePath(e)))
      .foldLeft(Json.obj())(_ + _)
    dir.addFile(JsonFileName).write(json.toString)
  }
  private def deleteJson(dir: DirectoryRef): Unit =
    dir.getFile(JsonFileName).map(_.delete.ensuring(x => x))
}
