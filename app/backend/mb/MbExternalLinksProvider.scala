package backend.mb

import java.io.File

import backend.external.{ExternalLink, ExternalLinkProvider, ExternalLinks, ExternalLinksProvider}
import backend.recon.Reconcilable._
import backend.recon.{Album, Artist, ReconID, ReconcilerCacher}
import common.RichFuture._
import models.Song

import scala.concurrent.{ExecutionContext, Future}

class MbExternalLinksProvider(implicit ec: ExecutionContext) extends ExternalLinksProvider {
  private val artistLinkExtractor = new ArtistLinkExtractor
  private val artistReconciler = new MbArtistReconcilerCacher
  private val albumLinkExtractor = new AlbumLinkExtractor
  private val albumReconciler = new ReconcilerCacher[Album](AlbumReconStorage, new MbAlbumReconciler(artistReconciler(_).map(_._1.get)))

  private def get[T](t: T,
                     reconciler: T => Future[(Option[ReconID], Boolean)],
                     linkExtractor: ExternalLinkProvider): Future[Traversable[ExternalLink]] =
    reconciler(t)
      .filterWith(_._1.isDefined, s"Couldn't reconcile <$t>")
      .map(_._1.get)
      .flatMap(linkExtractor.apply)

  private def getArtist(a: Artist) = get(a, artistReconciler, artistLinkExtractor)
  private def getAlbum(a: Album) = get(a, albumReconciler, albumLinkExtractor)

  override def getExternalLinks(s: Song): Future[ExternalLinks] =
    getArtist(s.artist)
      .zip(getAlbum(s.release))
      .map(e => ExternalLinks(e._1, e._2, Nil))
}

object MbExternalLinksProvider {
  import scala.concurrent.ExecutionContext.Implicits.global

  def main(args: Array[String]) {
    val $ = new MbExternalLinksProvider()
    val x = $.getExternalLinks(Song(new File("""D:\Media\Music\Metal\Black Metal\Rotting Christ\2007 Theogonia\01 - The Sign of Prime Creation.flac"""))).get
    println(x)
  }
}
