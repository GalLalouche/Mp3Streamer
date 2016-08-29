package backend.external

import backend.Url
import backend.configs.TestConfiguration
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import common.rich.RichFuture._
import org.scalatest.FreeSpec

import scala.concurrent.Future

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  private implicit val c = new TestConfiguration
  private val existingLink: ExternalLink[Album] = ExternalLink(Url("existing"), Host("host", Url("hosturl")))
  private val newLink: ExternalLink[Album] = ExternalLink(Url("new"), Host("newhost", Url("newhosturl")))
  private val anotherNewLink: ExternalLink[Album] = ExternalLink(Url("new2"), Host("newhost2", Url("newhosturl2")))
  private val expectedNewLinks: Links[Album] = List(ExternalLink(Url("new"), Host("newhost*", Url("newhosturl"))),
    ExternalLink(Url("new2"), Host("newhost2*", Url("newhosturl2"))))
  "should add * to new links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(newLink),
      x => Future successful List(anotherNewLink))
    $(null).get shouldReturn Set(existingLink) ++ expectedNewLinks
  }
  "should not add * to existing links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(newLink, existingLink),
      x => Future successful List(existingLink))
    $(null).get shouldReturn Set(existingLink, expectedNewLinks.head)
  }
  "should ignore different links from the same host" in {
    val newLinkButWithSameHost: ExternalLink[Album] =
      ExternalLink(Url("existing2"), Host("host", Url("hosturl")))
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(existingLink, newLinkButWithSameHost),
      x => Future successful List(newLinkButWithSameHost))
    $(null).get shouldReturn Set(existingLink)
  }
  "should not fail when there are multiple entries with the same host in existing" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink, existingLink.copy(link = Url("existing2"))),
      x => Future successful List(newLink, existingLink),
      x => Future successful List(existingLink))
    $(null).get should have size 3
  }
}
