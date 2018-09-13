package backend.external

import backend.Url
import backend.external.Host.{AllMusic, RateYourMusic, Wikipedia}
import backend.external.expansions.ExternalLinkExpander
import backend.external.recons.{LinkRetriever, LinkRetrievers}
import backend.module.TestModuleConfiguration
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import common.rich.RichT._
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.FreeSpec

import scala.concurrent.{ExecutionContext, Future}

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  private val injector = TestModuleConfiguration().injector
  private implicit val ec: ExecutionContext = injector.instance[ExecutionContext]
  private val existingHost: Host = Host("existinghost", Url("existinghosturl"))
  private val existingLink: BaseLink[Album] = BaseLink(Url("existing"), existingHost)
  private val existingMarkedLink: MarkedLink[Album] = MarkedLink markExisting existingLink
  private val rehashedLinks: BaseLink[Album] = existingLink.copy(link = Url("shouldbeignored"))
  private val expandedLink: BaseLink[Album] = BaseLink(Url("new"), Host("newhost", Url("newhosturl")))
  private val reconciledLink: BaseLink[Album] = BaseLink(Url("new2"), Host("newhost2", Url("newhosturl2")))
  private val markedReconciledLink = MarkedLink[Album](Url("new2"), Host("newhost2", Url("newhosturl2")), isNew = true)
  private val expectedNewLinks: List[MarkedLink[Album]] = List(
    MarkedLink(Url("new"), Host("newhost", Url("newhosturl")), isNew = true),
    markedReconciledLink)
  private def constExpander(links: BaseLink[Album]*) = new ExternalLinkExpander[Album] {
    override def sourceHost: Host = existingHost
    override def potentialHostsExtracted: Traversable[Host] = links.map(_.host)
    override def expand = Future.successful(links).const
  }
  private def constReconciler(_host: Host, link: BaseLink[Album]) = new LinkRetriever[Album] {
    override val host = _host
    override def apply(v1: Album) = Future successful Some(link)
  }
  private val newLinkExpander = constExpander(expandedLink)
  private val newLinkReconciler = constReconciler(reconciledLink.host, reconciledLink)
  private def constFuture[T](t: T) = Future.successful(t).const
  "should mark new links" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      List(existingLink) |> constFuture,
      LinkRetrievers(List(newLinkReconciler)),
      List(newLinkExpander),
    )
    $(null).get shouldReturn (Set(existingMarkedLink) ++ expectedNewLinks)
  }
  "Doesn't invoke hosts if there's no need" - {
    val failed = Future failed new AssertionError("Shouldn't have been invoked")
    def failedExpander(h: Host) = new ExternalLinkExpander[Album] {
      override val sourceHost: Host = existingHost
      override val potentialHostsExtracted: Traversable[Host] = List(h)
      override def expand = failed.const
    }
    def failedReconciler(_host: Host) = new LinkRetriever[Album] {
      override val host = _host
      override def apply(a: Album) = failed
    }
    "Should not invoke on existing hosts" in {
      val $ = new ExternalPipe[Album](
        ReconID("foobar") |> constFuture,
        List(existingLink) |> constFuture,
        LinkRetrievers(List(failedReconciler(existingHost), newLinkReconciler)),
        List(failedExpander(existingHost), newLinkExpander),
      )
      $(null).get shouldSetEqual Set(existingMarkedLink) ++ expectedNewLinks
    }
    "Should not invoke expanders if reconcilers already returned the host" in {
      val $ = new ExternalPipe[Album](
        ReconID("foobar") |> constFuture,
        List(existingLink) |> constFuture,
        LinkRetrievers(List(newLinkReconciler)),
        List(failedExpander(reconciledLink.host)),
      )
      $(null).get shouldSetEqual Set(existingMarkedLink, markedReconciledLink)
    }
  }
  "Should ignored new, extra links" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      List(existingLink) |> constFuture,
      LinkRetrievers(List(newLinkReconciler)),
      List(constExpander(expandedLink, rehashedLinks)),
    )
    $(null).get shouldSetEqual Set(existingMarkedLink) ++ expectedNewLinks
  }
  "Should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      List[BaseLink[Album]](existingLink, existingLink.copy(link = Url("existing2"))) |> constFuture,
      LinkRetrievers(List(newLinkReconciler)),
      List(newLinkExpander),
    )
    $(null).get should have size 4
  }
  "Should apply its finders recursively, but once at most" in {
    val wikiLink = BaseLink[Album](Url("wiki"), Wikipedia)
    val allMusicLink = BaseLink[Album](Url("amg"), AllMusic)
    val rateYouMusicLink = BaseLink[Album](Url("rym"), RateYourMusic)
    val wikiReconciler = constReconciler(Wikipedia, wikiLink)
    def oneTimeExpander(source: BaseLink[Album], dest: BaseLink[Album]) = new ExternalLinkExpander[Album] {
      private var firstRun = true
      override def potentialHostsExtracted: Traversable[Host] = List(dest.host)
      override def sourceHost: Host = source.host
      override def expand = v1 =>
        if (firstRun) {
          firstRun = false
          Future successful (if (v1 == source) List(dest) else Nil)
        }
        else Future failed new AssertionError(s"Expander from <$source> to <$dest> was invoke more than once")
    }

    val expander1 = oneTimeExpander(wikiLink, allMusicLink)
    val expander2 = oneTimeExpander(allMusicLink, rateYouMusicLink)
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      List(existingLink) |> constFuture,
      LinkRetrievers(List(wikiReconciler, newLinkReconciler)),
      List(expander1, expander2),
    )
    val expectedNewLinks: List[MarkedLink[Album]] =
      List(wikiLink, allMusicLink, rateYouMusicLink, reconciledLink).map(MarkedLink.markNew)
    $(null).get shouldSetEqual (Set(existingMarkedLink) ++ expectedNewLinks)
  }
}
