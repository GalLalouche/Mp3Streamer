package mains.cover.image

import backend.Url
import backend.external.DocumentSpecs
import mains.cover.UrlSource
import org.scalatest.FreeSpec
import org.scalatest.OptionValues._

class NewParserTest extends FreeSpec with DocumentSpecs {
  private def test(doc: String, expectedUrlSources: UrlSource*) =
    NewParser(getDocument(doc + ".html")).value should contain allElementsOf (expectedUrlSources)

  "parse images on new format" in {
    test("new",
      UrlSource(Url("https://img.discogs.com/bvYutplspRMXd6t-9g9ySxz4Sdo=/fit-in/600x600/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-2276425-1320153950.jpeg.jpg"), 600, 600),
      UrlSource(Url("https://www.music-bazaar.com/album-images/vol1002/489/489829/2320587-big/Through-These-Eyes-cover.jpg"), 3050, 3050),
      UrlSource(Url("https://monster-podcast.com/zodiac/wp-content/uploads/sites/3/2019/02/ep-4-mask-lake-web.jpg"), 1080, 1080),
      UrlSource(Url("http://www.mostly-autumn.com/mostlyautumnrecords/3/products/White_Rainbow/WhiteRainbowLimitedEdition.gif"), 451, 451),
    )
  }
  "Handle null values" in {
    test("new_null",
      UrlSource(Url("http://www.disccenter.co.il/content/products/prodimage_34217.jpg"), 300, 300),
      UrlSource(Url("http://stereo-ve-mono.com/sleeves/09/0917501b.jpg"), 300, 300),
      UrlSource(Url("https://s.mxmcdn.net/images-storage/albums/1/8/0/3/0/5/27503081_800_800.jpg"), 800, 800),
      UrlSource(Url("https://i1.sndcdn.com/artworks-000131200691-jcpqkn-t500x500.jpg"), 500, 500),
    )
  }
  "decodes head link" in {
    test("new_encoded",
      UrlSource(Url("https://upload.wikimedia.org/wikipedia/en/e/e4/Classica_%28album%29_cover.jpg"), 300, 300),
      UrlSource(Url("https://upload.wikimedia.org/wikipedia/en/thumb/e/e4/Classica_%28album%29_cover.jpg/220px-Classica_%28album%29_cover.jpg"), 220, 220),
      UrlSource(Url("https://metalitalia.com/wp-content/uploads/2018/08/novembre-classica-2000-500x500.jpg"), 500, 500),
      UrlSource(Url("https://images-na.ssl-images-amazon.com/images/I/41BH09SZQWL.jpg"), 301, 301),
    )
  }
  "fail on old format" in {
    NewParser(getDocument("old.html")) shouldReturn None
  }
}
