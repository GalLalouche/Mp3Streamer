package mains.cover.image.scrappa

import mains.cover.image.ImageApiSourceTest
import play.api.libs.json.Json

class ScrappaTest
    extends ImageApiSourceTest(
      ScrappaModule,
      Json.parse(classOf[ScrappaTest].getResourceAsStream("test.json")),
    )
