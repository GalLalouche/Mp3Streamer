package backend.external.expansions

import backend.Url
import backend.external.Host
import backend.recon.Album
import org.jsoup.nodes.Document

import scala.concurrent.Future

private trait DocumentToAlbumParser {
  def host: Host
  def findAlbum(d: Document, a: Album): Future[Option[Url]]
  def modifyUrl(u: Url, a: Album): Url = u
}
