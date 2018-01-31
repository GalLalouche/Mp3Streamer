package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.Reconciler
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import org.scalatest.FreeSpec

import scala.concurrent.Future

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingHost: Host = Host("existinghost", Url("existinghosturl"))
  private val existingLink: BaseLink[Album] = BaseLink(Url("existing"), existingHost)
  private val existingMarkedLink: MarkedLink[Album] = MarkedLink markExisting existingLink
  private val rehashedLinks: BaseLink[Album] = existingLink.copy(link = Url("shouldbeignored"))
  private val expandedLink: BaseLink[Album] = BaseLink(Url("new"), Host("newhost", Url("newhosturl")))
  private val reconciledLink: BaseLink[Album] = BaseLink(Url("new2"), Host("newhost2", Url("newhosturl2")))
  private val expectedNewLinks: List[MarkedLink[Album]] =
    List(MarkedLink(Url("new"), Host("newhost", Url("newhosturl")), isNew = true),
      MarkedLink(Url("new2"), Host("newhost2", Url("newhosturl2")), isNew = true))
  private def constExpander(links: BaseLink[Album]*) = new ExternalLinkExpander[Album] {
    override def sourceHost: Host = existingHost
    override def potentialHostsExtracted: Traversable[Host] = links.map(_.host)
    override def apply(v1: BaseLink[Album]): Future[BaseLinks[Album]] = Future successful links
  }
  private val newLinkExpander = constExpander(expandedLink)
  private val newLinkReconciler = new Reconciler[Album](reconciledLink.host) {
    override def apply(a: Album) = Future successful Some(reconciledLink)
  }
  private def constFuture[T](t: T): Any => Future[T] = _ => Future successful t
  "should mark new links" in {
    val $ = new ExternalPipe[Album](ReconID("foobar") |> constFuture,
      List(existingLink) |> constFuture,
      List(newLinkExpander),
      List(newLinkReconciler))
    $(null).get shouldReturn (Set(existingMarkedLink) ++ expectedNewLinks)
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
        override def apply(v1: BaseLink[Album]): Future[BaseLinks[Album]] = failed
      }
      val $ = new ExternalPipe[Album](ReconID("foobar") |> constFuture,
        List(existingLink) |> constFuture,
        List(failedExpander, newLinkExpander),
        List(failedReconciler(existingHost), newLinkReconciler))
      $(null).get shouldReturn Set(existingMarkedLink) ++ expectedNewLinks
    }
    "Should not invoke additional reconcilers if expanders already returned the host" in {
      val $ = new ExternalPipe[Album](ReconID("foobar") |> constFuture,
        List(existingLink) |> constFuture,
        List(newLinkExpander),
        List(failedReconciler(expandedLink.host)))
      $(null).get shouldReturn Set(existingMarkedLink) ++ expectedNewLinks.take(1)
    }
  }
  "should ignored new, extra links" in {
    val $ = new ExternalPipe[Album](ReconID("foobar") |> constFuture,
      List(existingLink) |> constFuture,
      List(constExpander(expandedLink, rehashedLinks)),
      List(newLinkReconciler))
    $(null).get shouldReturn Set(existingMarkedLink) ++ expectedNewLinks

  }
  "should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](ReconID("foobar") |> constFuture,
      List[BaseLink[Album]](existingLink, existingLink.copy(link = Url("existing2"))) |> constFuture,
      List(newLinkExpander),
      List(newLinkReconciler))
    $(null).get should have size 4
  }
}
