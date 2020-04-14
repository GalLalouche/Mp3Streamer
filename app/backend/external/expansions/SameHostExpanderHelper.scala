package backend.external.expansions

import backend.external.BaseLink
import backend.recon.{Album, Artist}
import backend.FutureOption
import javax.inject.Inject

import scala.concurrent.ExecutionContext

import scalaz.std.scalaFuture.futureInstance
import common.rich.func.RichOptionT._

import common.io.InternetTalker

private class SameHostExpanderHelper @Inject()(it: InternetTalker) {
  private implicit val iec: ExecutionContext = it

  def apply(documentToAlbumParser: DocumentToAlbumParser)(
      e: BaseLink[Artist], a: Album): FutureOption[BaseLink[Album]] =
    it.downloadDocument(documentToAlbumParser.modifyUrl(e.link, a)).liftSome
        .flatMap(documentToAlbumParser.findAlbum(_, a))
        .map(BaseLink[Album](_, documentToAlbumParser.host))
}
