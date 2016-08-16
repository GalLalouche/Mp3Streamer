package src.backend.external

import backend.TestConfiguration._
import backend.Url
import backend.external.{ExternalLink, ExternalPipe, Host}
import backend.recon.{Album, ReconID}
import common.AuxSpecs
import org.scalatest.FreeSpec

import scala.concurrent.Future
import common.RichFuture._

class ExternalPipeTest extends FreeSpec with AuxSpecs {
  val existingLink: ExternalLink[Album] = ExternalLink(Url("existing"), Host("host", Url("hosturl")))
  val newLink: ExternalLink[Album] = ExternalLink(Url("new"), Host("newhost", Url("newhosturl")))
  val expectedNewLink: ExternalLink[Album] = ExternalLink(Url("new"), Host("newhost*", Url("newhosturl")))
  "should add * to new links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(newLink))
    $(null).get shouldReturn Set(existingLink, expectedNewLink)
  }
  "should not add * to existing links" in {
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(newLink, existingLink))
    $(null).get shouldReturn Set(existingLink, expectedNewLink)
  }
  "should ignore different links from the same host" in {
    val newLinkButWithSameHost: ExternalLink[Album] =
      ExternalLink(Url("existing2"), Host("host", Url("hosturl")))
    val $ = new ExternalPipe[Album](x => Future successful ReconID("foobar"),
      x => Future successful List(existingLink),
      x => Future successful List(existingLink, newLinkButWithSameHost))
    $(null).get shouldReturn Set(existingLink)
  }
}
