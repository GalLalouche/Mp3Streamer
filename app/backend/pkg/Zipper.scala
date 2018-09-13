package backend.pkg

import backend.Retriever
import common.io.{DirectoryRef, FileRef, FolderCleaner, RootDirectory}
import common.rich.func.ToMoreFoldableOps
import controllers.UrlPathUtils
import javax.inject.Inject
import models.MusicFinder
import play.api.libs.json.{Json, JsString}

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process._

import scalaz.std.{FutureInstances, OptionInstances}
import scalaz.syntax.ToApplicativeOps

private class Zipper @Inject()(
    ec: ExecutionContext,
    mf: MusicFinder,
    @RootDirectory rootDirectory: DirectoryRef,
) extends Retriever[DirectoryRef, FileRef]
    with ToMoreFoldableOps with OptionInstances
    with ToApplicativeOps with FutureInstances {
  import Zipper._

  private implicit val iec: ExecutionContext = ec
  private val zipsDir = rootDirectory.addSubDir("zips")
  private val createJsonIn = createRemotePathJson(mf) _
  private val cleaner = new FolderCleaner(zipsDir)

  override def apply(dir: DirectoryRef): Future[FileRef] = Future {
    val outputName = dir.name + ".zip"
    zipsDir.getFile(outputName).getOrElse(try {
      createJsonIn(dir)
      Seq(ZipAppPath, "a", "-r", "-mx0", s"${zipsDir.path}/$outputName", dir.path + "/*").!!
      zipsDir.getFile(outputName).get
    } finally
      deleteJson(dir)
    )
  } <* cleaner.!()
}

private object Zipper {
  private val ZipAppPath = """c:\Program Files\7-Zip\7z.exe"""
  private[this] val JsonFileName = "remote_paths.json"

  private def createRemotePathJson(mf: MusicFinder)(
      dir: DirectoryRef): Unit = {
    val json = mf.getSongsInDir(dir)
        .map(e => e.file.name -> JsString(UrlPathUtils.encodePath(e)))
        .foldLeft(Json.obj())(_ + _)
    dir.addFile(JsonFileName).write(json.toString)
  }
  private def deleteJson(dir: DirectoryRef): Unit = dir.getFile(JsonFileName).map(_.delete.ensuring(x => x))
}
