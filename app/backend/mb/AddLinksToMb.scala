package backend.mb

import backend.configs.StandaloneConfig
import backend.external.ExternalLink
import backend.external.extensions.AlbumLinksExpander
import backend.recon.{Album, ReconID}
import common.rich.RichT._
import common.rich.RichFuture._
import common.rich.collections.RichSet._

import scala.concurrent.Future

object AddLinksToMb {
  private implicit val c = StandaloneConfig
  type Links = Traversable[ExternalLink[Album]]
  type FLinks = Future[Links]
  private val externalLinkProvider = new AlbumLinkExtractor()
  val expender = new AlbumLinksExpander()
  def reconcileAlbum(a: Album) = "8a061d0e-fa0c-4571-91ff-8bc3911b6428" |> ReconID
  def getExistingLinks(a: Album): FLinks = a |> reconcileAlbum |> externalLinkProvider
  def expend(existing: Links): FLinks = expender(existing)
  def apply(a: Album): FLinks =
    for (existing <- getExistingLinks(a); expended <- expend(existing))
      yield expended.toSet \ existing.toSet

  def main(args: Array[String]) {
    println("Found new links: " + apply(null).get.mkString("\n"))
  }
}
