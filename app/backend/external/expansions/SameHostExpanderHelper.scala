package backend.external.expansions

import backend.external.BaseLink
import backend.recon.{Album, Artist}
import backend.FutureOption
import common.io.InternetTalker
import javax.inject.Inject

import scala.concurrent.ExecutionContext

private class SameHostExpanderHelper @Inject()(it: InternetTalker) {
  private implicit val iec: ExecutionContext = it

  def apply(documentToAlbumParser: DocumentToAlbumParser)(
      e: BaseLink[Artist], a: Album): FutureOption[BaseLink[Album]] =
    it.downloadDocument(documentToAlbumParser.modifyUrl(e.link, a))
        .flatMap(documentToAlbumParser.findAlbum(_, a))
        .map(_.map(BaseLink[Album](_, documentToAlbumParser.host)))
}
