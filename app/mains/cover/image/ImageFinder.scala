package mains.cover.image

import backend.Url
import javax.inject.Inject
import common.rich.RichFuture._
import mains.cover.UrlSource

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import common.io.{DirectoryRef, InternetTalker, RootDirectory}

private[cover] class ImageFinder @Inject()(it: InternetTalker, @RootDirectory dumpDir: DirectoryRef) {
  private implicit val iec: ExecutionContext = it
  private val parser = HtmlParser.composite(dumpDir, OldParser, NewParser)
  def find(url: Url): Future[Try[Seq[UrlSource]]] = it.downloadDocument(url).map(parser.apply).toTry
}
