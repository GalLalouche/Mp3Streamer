package backend.external.expansions

import backend.FutureOption
import backend.external.BaseLink
import backend.recon.{Album, Artist}
import com.google.inject.Inject

import scala.concurrent.ExecutionContext

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._

import common.io.InternetTalker

private class SameHostExpanderHelper @Inject() (it: InternetTalker) {
  private implicit val iec: ExecutionContext = it

  def apply(
      documentToAlbumParser: DocumentToAlbumParser,
  )(e: BaseLink[Artist], a: Album): FutureOption[BaseLink[Album]] =
    it.downloadDocument(documentToAlbumParser.modifyUrl(e.link, a))
      .liftSome
      .flatMap(documentToAlbumParser.findAlbum(_, a))
      .map(BaseLink[Album](_, documentToAlbumParser.host))
}
