package backend.external.expansions

import javax.inject.Inject
import scala.concurrent.ExecutionContext

import backend.external.BaseLink
import backend.recon.{Album, Artist}
import backend.FutureOption
import common.io.InternetTalker
import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT._

private class SameHostExpanderHelper @Inject() (it: InternetTalker) {
  private implicit val iec: ExecutionContext = it

  def apply(
      documentToAlbumParser: DocumentToAlbumParser,
  )(e: BaseLink[Artist], a: Album): FutureOption[BaseLink[Album]] =
    it.downloadDocument(documentToAlbumParser.modifyUrl(e.link, a).toLemonLabs)
      .liftSome
      .flatMap(documentToAlbumParser.findAlbum(_, a))
      .map(BaseLink[Album](_, documentToAlbumParser.host))
}
