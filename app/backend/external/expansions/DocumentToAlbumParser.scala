package backend.external.expansions

import backend.FutureOption
import backend.external.Host
import backend.recon.Album
import io.lemonlabs.uri.Url
import org.jsoup.nodes.Document

private trait DocumentToAlbumParser {
  def host: Host
  def findAlbum(d: Document, a: Album): FutureOption[Url]
  def modifyUrl(u: Url, a: Album): Url = u
}
