package mains.cover.image

import backend.Url
import org.scalatest.OptionValues._
import backend.external.DocumentSpecs
import mains.cover.UrlSource
import org.scalatest.FreeSpec

class NewParserTest extends FreeSpec with DocumentSpecs {
  "parse images old new format" in {
    NewParser(getDocument("new.html")).value should contain allOf(
        UrlSource(Url("https://img.discogs.com/bvYutplspRMXd6t-9g9ySxz4Sdo=/fit-in/600x600/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-2276425-1320153950.jpeg.jpg"), 600, 600),
        UrlSource(Url("https://www.music-bazaar.com/album-images/vol1002/489/489829/2320587-big/Through-These-Eyes-cover.jpg"), 3050, 3050),
        UrlSource(Url("https://monster-podcast.com/zodiac/wp-content/uploads/sites/3/2019/02/ep-4-mask-lake-web.jpg"), 1080, 1080),
        UrlSource(Url("http://www.mostly-autumn.com/mostlyautumnrecords/3/products/White_Rainbow/WhiteRainbowLimitedEdition.gif"), 451, 451),
    )
  }
  "fail on old format" in {
    NewParser(getDocument("old.html")) shouldReturn None
  }
}
