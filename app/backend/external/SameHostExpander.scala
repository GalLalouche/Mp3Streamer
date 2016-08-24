package backend.external

import backend.Url
import backend.recon.{Album, Artist}
import backend.storage.Retriever
import common.io.DocumentDownloader
import org.jsoup.nodes.Document

import scala.concurrent.{ExecutionContext, Future}

/** E.g., from an artist's wikipedia page, to that artists' wikipedia pages of her albums */
private abstract class SameHostExpander(val host: Host)(implicit ec: ExecutionContext)
    extends ((ExternalLink[Artist], Album) => Future[Option[ExternalLink[Album]]]) {
  protected def aux(d: Document, a: Album): Option[Url]

  protected def fromUrl(u: Url, a: Album): Future[Option[ExternalLink[Album]]] =
    DocumentDownloader(u)
        .map(aux(_, a).map(ExternalLink[Album](_, host)))

  override def apply(e: ExternalLink[Artist], a: Album) = fromUrl(e.ensuring(_.host == host).link, a)
}
