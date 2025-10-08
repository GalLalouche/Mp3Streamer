package backend.external

import backend.external.Host.{AllMusic, RateYourMusic, Wikipedia}
import backend.external.expansions.ExternalLinkExpander
import backend.external.mark.ExternalLinkMarker
import backend.external.recons.{LinkRetriever, LinkRetrievers}
import backend.recon.{Album, ReconID}
import io.lemonlabs.uri.Url
import org.scalatest.AsyncFreeSpec

import scala.concurrent.Future

import common.rich.func.BetterFutureInstances._
import common.rich.func.RichOptionT
import scalaz.OptionT

import common.rich.RichT._
import common.test.AuxSpecs

class ExternalPipeTest extends AsyncFreeSpec with AuxSpecs {
  private val existingHost: Host = Host("existinghost", Url.parse("existinghosturl"))
  private val existingLink: BaseLink[Album] = BaseLink(Url.parse("existing"), existingHost)
  private val existingMarkedLink: MarkedLink[Album] = MarkedLink.markExisting(existingLink)
  private val rehashedLinks: BaseLink[Album] =
    existingLink.copy(link = Url.parse("shouldbeignored"))
  private val expandedLink: BaseLink[Album] =
    BaseLink(Url.parse("new"), Host("newhost", Url.parse("newhosturl")))
  private val reconciledLink: BaseLink[Album] =
    BaseLink(Url.parse("new2"), Host("newhost2", Url.parse("newhosturl2")))
  private val markedReconciledLink =
    MarkedLink[Album](Url.parse("new2"), Host("newhost2", Url.parse("newhosturl2")), LinkMark.New)
  private val expectedNewLinks = Vector(
    MarkedLink[Album](Url.parse("new"), Host("newhost", Url.parse("newhosturl")), LinkMark.New),
    markedReconciledLink,
  )
  private def constExpander(links: BaseLink[Album]*) = new ExternalLinkExpander[Album] {
    override def sourceHost: Host = existingHost
    override def potentialHostsExtracted: Iterable[Host] = links.map(_.host)
    override def expand = Future.successful(links).const
  }
  private def constReconciler(_host: Host, link: BaseLink[Album]) = new LinkRetriever[Album] {
    override val host = _host
    override val qualityRank = 0
    override def apply(v1: Album) = RichOptionT.pointSome[Future].apply(link)
  }
  private val newLinkExpander = constExpander(expandedLink)
  private val newLinkReconciler = constReconciler(reconciledLink.host, reconciledLink)
  private val marker = constReconciler(reconciledLink.host, reconciledLink)
  private def constFuture[T](t: T) = Future.successful(t).const
  "should mark new links" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      Vector(existingLink) |> constFuture,
      LinkRetrievers(newLinkReconciler),
      Vector(newLinkExpander),
      Nil,
    )
    $(null).map(_ shouldMultiSetEqual (existingMarkedLink +: expectedNewLinks))
  }
  "Doesn't invoke hosts if there's no need" - {
    val failed = Future.failed(new AssertionError("Shouldn't have been invoked"))
    def failedExpander(h: Host) = new ExternalLinkExpander[Album] {
      override val sourceHost: Host = existingHost
      override val potentialHostsExtracted: Iterable[Host] = Vector(h)
      override def expand = failed.const
    }
    def failedReconciler(_host: Host) = new LinkRetriever[Album] {
      override val host = _host
      override val qualityRank = Int.MaxValue
      override def apply(a: Album) = OptionT[Future, BaseLink[Album]](failed)
    }
    "Should not invoke on existing hosts" in {
      val $ = new ExternalPipe[Album](
        ReconID("foobar") |> constFuture,
        Vector(existingLink) |> constFuture,
        LinkRetrievers(failedReconciler(existingHost), newLinkReconciler),
        Vector(failedExpander(existingHost), newLinkExpander),
        Nil,
      )
      $(null).map(_ shouldMultiSetEqual (existingMarkedLink +: expectedNewLinks))
    }
    "Should not invoke expanders if reconcilers already returned the host" in {
      val $ = new ExternalPipe[Album](
        ReconID("foobar") |> constFuture,
        Vector(existingLink) |> constFuture,
        LinkRetrievers(newLinkReconciler),
        Vector(failedExpander(reconciledLink.host)),
        Nil,
      )
      $(null).map(_ shouldContainExactly (existingMarkedLink, markedReconciledLink))
    }
  }
  "Should ignored new, extra links" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      Vector(existingLink) |> constFuture,
      LinkRetrievers(newLinkReconciler),
      Vector(constExpander(expandedLink, rehashedLinks)),
      Nil,
    )
    $(null).map(_ shouldMultiSetEqual (existingMarkedLink +: expectedNewLinks))
  }
  "Should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      Vector[BaseLink[Album]](
        existingLink,
        existingLink.copy(link = Url.parse("existing2")),
      ) |> constFuture,
      LinkRetrievers(newLinkReconciler),
      Vector(newLinkExpander),
      Nil,
    )
    $(null).map(_ should have size 4)
  }
  "Should apply its finders recursively, but once at most" in {
    val wikiLink = BaseLink[Album](Url.parse("wiki"), Wikipedia)
    val allMusicLink = BaseLink[Album](Url.parse("amg"), AllMusic)
    val rateYouMusicLink = BaseLink[Album](Url.parse("rym"), RateYourMusic)
    val wikiReconciler = constReconciler(Wikipedia, wikiLink)
    def oneTimeExpander(source: BaseLink[Album], dest: BaseLink[Album]) =
      new ExternalLinkExpander[Album] {
        private var firstRun = true
        override def potentialHostsExtracted: Iterable[Host] = Vector(dest.host)
        override def sourceHost: Host = source.host
        override def expand = v1 =>
          if (firstRun) {
            firstRun = false
            Future.successful(if (v1 == source) Vector(dest) else Nil)
          } else
            Future.failed(
              new AssertionError(s"Expander from <$source> to <$dest> was invoke more than once"),
            )
      }

    val expander1 = oneTimeExpander(wikiLink, allMusicLink)
    val expander2 = oneTimeExpander(allMusicLink, rateYouMusicLink)
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      Vector(existingLink) |> constFuture,
      LinkRetrievers(wikiReconciler, newLinkReconciler),
      Vector(expander1, expander2),
      Nil,
    )
    val expectedNewLinks: Vector[MarkedLink[Album]] =
      Vector(wikiLink, allMusicLink, rateYouMusicLink, reconciledLink).map(MarkedLink.markNew)
    $(null).map(_ shouldMultiSetEqual (existingMarkedLink +: expectedNewLinks))
  }

  "should apply extra markers" in {
    val marker = new ExternalLinkMarker[Album] {
      override def host = markedReconciledLink.host
      override def apply(l: MarkedLink[Album]) = Future.successful(LinkMark.Text("foobar"))
    }
    val $ = new ExternalPipe[Album](
      ReconID("foobar") |> constFuture,
      Vector(existingLink) |> constFuture,
      LinkRetrievers(newLinkReconciler),
      Vector(newLinkExpander),
      Vector(marker),
    )
    val expectedNewLinks = Vector(
      MarkedLink[Album](Url.parse("new"), Host("newhost", Url.parse("newhosturl")), LinkMark.New),
      MarkedLink[Album](
        Url.parse("new2"),
        Host("newhost2", Url.parse("newhosturl2")),
        LinkMark.Text("foobar"),
      ),
    )
    $(null).map(_ shouldMultiSetEqual (existingMarkedLink +: expectedNewLinks))
  }
}
