package mains.cover.image.serp

import mains.cover.image.ImageApiSourceTest
import play.api.libs.json.Json

class SerpTest
    extends ImageApiSourceTest(
      SerpModule,
      Json.parse(classOf[SerpTest].getResourceAsStream("test.json")),
    )
