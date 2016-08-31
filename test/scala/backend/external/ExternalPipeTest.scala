package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.external.extensions.ExternalLinkExpander
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import org.jsoup.nodes.Document
import org.scalatest.FreeSpec

import scala.concurrent.Future

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingHost: Host = Host("existinghost", Url("existinghosturl"))
  private val existingLink: ExternalLink[Album] = ExternalLink(Url("existing"), existingHost)
  private val expandedLink: ExternalLink[Album] = ExternalLink(Url("new"), Host("newhost", Url("newhosturl")))
  private val reconciledLink: ExternalLink[Album] = ExternalLink(Url("new2"), Host("newhost2", Url("newhosturl2")))
  private val expectedNewLinks: Links[Album] = List(ExternalLink(Url("new"), Host("newhost*", Url("newhosturl"))),
    ExternalLink(Url("new2"), Host("newhost2*", Url("newhosturl2"))))
  val constReconciler = new Reconciler[Album](reconciledLink.host) {
    override def apply(a: Album) = Future successful Some(reconciledLink)
  }
  //TODO extract a trait
  val constExpander = new ExternalLinkExpander[Album](existingHost, List(expandedLink.host)) {
    override protected def aux(d: Document): Links[Album] = ???
    override def apply(v1: ExternalLink[Album]): Future[Links[Album]] = Future successful List(expandedLink)

  }
  "should add * to new links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      List(constExpander),
      List(constReconciler))
    $(null).get shouldReturn (Set(existingLink) ++ expectedNewLinks)
  }
  "should not invoke on existing hosts" in {
    val failed = Future failed new AssertionError("Shouldn't have been invoked")
    val failedReconciler = new Reconciler[Album](existingHost) {
      override def apply(a: Album) = failed
    }
    val failedExpander = new ExternalLinkExpander[Album](existingHost, List(existingHost)) {
      override protected def aux(d: Document): Links[Album] = ???
      override def apply(v1: ExternalLink[Album]): Future[Links[Album]] = failed
    }
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      List(failedExpander, constExpander),
      List(failedReconciler, constReconciler))
    $(null).get shouldReturn Set(existingLink) ++ expectedNewLinks
  }
  "should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink, existingLink.copy(link = Url("existing2"))),
      List(constExpander),
      List(constReconciler))
    $(null).get should have size 4
  }
}
