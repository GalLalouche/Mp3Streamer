package backend.external

import backend.Url

case class RelativeUrl(relativePath: String, host: Host) {
  def toUrl: Url = host.url +/ relativePath
}
