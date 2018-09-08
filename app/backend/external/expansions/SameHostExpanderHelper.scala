package backend.external.expansions

import backend.external.BaseLink
import backend.recon.{Album, Artist}
import common.io.InternetTalker
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

private class SameHostExpanderHelper @Inject()(it: InternetTalker) {
  private implicit val iec: ExecutionContext = it

  def apply(documentToAlbumParser: DocumentToAlbumParser)(
      e: BaseLink[Artist], a: Album): Future[Option[BaseLink[Album]]] =
    it.downloadDocument(documentToAlbumParser.modifyUrl(e.link, a))
        .flatMap(documentToAlbumParser.findAlbum(_, a))
        .map(a => a.map(BaseLink[Album](_, documentToAlbumParser.host)))
}