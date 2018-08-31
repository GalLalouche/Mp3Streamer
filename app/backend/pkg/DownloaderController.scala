package backend.pkg

import java.io.FileInputStream

import akka.stream.scaladsl.Source
import akka.util.ByteString
import backend.Retriever
import com.google.inject.Guice
import common.io.{DirectoryRef, FileRef, IODirectory, IOFile}
import common.rich.primitives.RichString._
import controllers.{ControllerUtils, LegacyController}
import net.codingwell.scalaguice.InjectorExtensions._
import play.api.http.HttpEntity
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.mvc.Action

import scala.concurrent.ExecutionContext

object DownloaderController extends LegacyController {
  private val injector = Guice.createInjector(c.module, PkgModule)
  private implicit val ec: ExecutionContext =  injector.instance[ExecutionContext]
  private val zipper: Retriever[DirectoryRef, FileRef] =
    injector.instance[ZipperFactory].apply(ControllerUtils.encodePath)

  def download(path: String) = Action.async {request =>
    // TODO fix code duplication with streamer
    def parseRange(s: String): Long = s.dropAfterLast('=').takeWhile(_.isDigit).toLong
    val bytesToSkip: Long = request.headers get "Range" map parseRange getOrElse 0L
    val file = ControllerUtils.parseFile(path)
    require(file.isDirectory)
    zipper(IODirectory(file.getAbsolutePath))
        .map(file => {
          val fis = new FileInputStream(file.asInstanceOf[IOFile].file)
          fis.skip(bytesToSkip)
          val status = if (bytesToSkip == 0) Ok else PartialContent
          val source =
            Source.fromPublisher(IterateeStreams.enumeratorToPublisher(Enumerator fromStream fis)).map(ByteString.apply)
          status.sendEntity(HttpEntity.Streamed(source, Some(file.size - bytesToSkip), Some("application/zip"))).withHeaders(
            "Access-Control-Allow-Headers" -> "range, accept-encoding",
            "Access-Control-Allow-Origin" -> "*",
            "Accept-Ranges" -> "bytes",
            "Connection" -> "keep-alive",
            //"Content-Length" -> s"${}",
            "Content-Range" -> s"bytes $bytesToSkip-${file.size}/${file.size}",
            "Content-Disposition" -> s"""attachment; filename="${file.name}"""",
          )
        })
  }
}
