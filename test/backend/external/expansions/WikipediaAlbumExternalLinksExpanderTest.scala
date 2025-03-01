package backend.external.expansions

import java.net.HttpURLConnection

import backend.external.{BaseLink, DocumentSpecs, Host}
import backend.module.{FakeWSResponse, TestModuleConfiguration}
import io.lemonlabs.uri.Url
import net.codingwell.scalaguice.InjectorExtensions._
import org.scalatest.AsyncFreeSpec

import common.rich.RichT._
import common.rich.collections.RichTraversableOnce._
import common.test.AsyncAuxSpecs

class WikipediaAlbumExternalLinksExpanderTest
    extends AsyncFreeSpec
    with DocumentSpecs
    with AsyncAuxSpecs {
  private val config: TestModuleConfiguration =
    TestModuleConfiguration().copy(_urlToBytesMapper = { case x => getBytes(x) })

  private val $ : WikipediaAlbumExternalLinksExpander =
    config.injector.instance[WikipediaAlbumExternalLinksExpander]

  "succeed even if there is no link" in {
    $.parseDocument(getDocument("no_link.html")) shouldReturn Nil
  }
  "extract allmusic link" in {
    $.parseDocument(getDocument("allmusic_link.html"))
      .filter(_.host == Host.AllMusic)
      .map(_.link.toStringPunycode)
      .single shouldReturn
      "http://www.allmusic.com/album/born-in-the-usa-mw0000191830"
  }
  "Return nothing on error" in {
    val $ = this.config
      .copy(_urlToResponseMapper =
        FakeWSResponse(status = HttpURLConnection.HTTP_INTERNAL_ERROR).partialConst,
      )
      .injector
      .instance[WikipediaAlbumExternalLinksExpander]
    $.expand(BaseLink(Url.parse("allmusic_rlink.html"), Host.Wikipedia)) shouldEventuallyReturn Nil
  }
}
