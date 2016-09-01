package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.Reconciler
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.FreeSpec

import scala.concurrent.Future

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingHost: Host = Host("existinghost", Url("existinghosturl"))
  private val existingLink: ExternalLink[Album] = ExternalLink(Url("existing"), existingHost)
  private val rehashedLinks: ExternalLink[Album] = existingLink.copy(link = Url("shouldbeignored"))
  private val expandedLink: ExternalLink[Album] = ExternalLink(Url("new"), Host("newhost", Url("newhosturl")))
  private val reconciledLink: ExternalLink[Album] = ExternalLink(Url("new2"), Host("newhost2", Url("newhosturl2")))
  private val expectedNewLinks: Links[Album] = List(ExternalLink(Url("new"), Host("newhost*", Url("newhosturl"))),
    ExternalLink(Url("new2"), Host("newhost2*", Url("newhosturl2"))))
  def constExpander(links: ExternalLink[Album]*) = new ExternalLinkExpander[Album] {
    override def sourceHost: Host = existingHost
    override def potentialHostsExtracted: Traversable[Host] = links.map(_.host)
    override def apply(v1: ExternalLink[Album]): Future[Links[Album]] = Future successful links
  }
  val newLinkExpander = constExpander(expandedLink)
  val newLinkReconciler = new Reconciler[Album](reconciledLink.host) {
    override def apply(a: Album) = Future successful Some(reconciledLink)
  }
  "should add * to new links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      List(newLinkExpander),
      List(newLinkReconciler))
    $(null).get shouldReturn (Set(existingLink) ++ expectedNewLinks)
  }
  "lazy" - {
    val failed = Future failed new AssertionError("Shouldn't have been invoked")
    def failedReconciler(host: Host) = new Reconciler[Album](host) {
      override def apply(a: Album) = failed
    }
    "should not invoke on existing hosts" in {
      val failedExpander = new ExternalLinkExpander[Album] {
        override val sourceHost: Host = existingHost
        override val potentialHostsExtracted: Traversable[Host] = List(existingHost)
        override def apply(v1: ExternalLink[Album]): Future[Links[Album]] = failed
      }
      val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
        x => Future successful List(existingLink),
        List(failedExpander, newLinkExpander),
        List(failedReconciler(existingHost), newLinkReconciler))
      $(null).get shouldReturn Set(existingLink) ++ expectedNewLinks
    }
    "Should not invoke additional reconcilers if expanders already returned the host" in {
      val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
        x => Future successful List(existingLink),
        List(newLinkExpander),
        List(failedReconciler(expandedLink.host)))
      $(null).get shouldReturn Set(existingLink) ++ expectedNewLinks.take(1)
    }
  }
  "should ignored new, extra links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      List(constExpander(expandedLink, rehashedLinks)),
      List(newLinkReconciler))
    $(null).get shouldReturn Set(existingLink) ++ expectedNewLinks

  }
  "should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink, existingLink.copy(link = Url("existing2"))),
      List(newLinkExpander),
      List(newLinkReconciler))
    $(null).get should have size 4
  }
}
