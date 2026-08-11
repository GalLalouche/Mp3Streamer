package mains.cover.image

import backend.module.TestModuleConfiguration
import com.google.inject.Guice
import common.guice.RichModule.richModule
import common.rich.RichT.lazyT
import common.test.AuxSpecs
import io.lemonlabs.uri.Url
import mains.cover.UrlSource
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import net.codingwell.scalaguice.ScalaModule
import org.scalatest.freespec.AsyncFreeSpec
import play.api.libs.json.Json

private abstract class ImageApiSourceTest[Api <: ImageAPI: Manifest]
    extends AsyncFreeSpec
    with AuxSpecs {
  private val json = Json.parse(manifest.runtimeClass.getResourceAsStream("test.json"))

  private val injector =
    Guice.createInjector(
      TestModuleConfiguration(_urlToBytesMapper = json.toString.getBytes.partialConst).module
        .overrideWith(new ScalaModule {
          override def configure(): Unit = bind[ImageAPI].to[Api]
        }),
    )
  private val $ = injector.instance[ImageSearch]
  "apply" in {
    $("terms", 1).toSeq.map(
      _.shouldContainExactly(
        UrlSource(
          Url.parse("https://upload.wikimedia.org/wikipedia/en/a/af/Opeth_Orchid.jpg"),
          width = 400,
          height = 300,
        ),
        UrlSource(
          Url.parse("https://img.discogs.com/discogs-images/R-5076240-1460985218-7752.jpeg.jpg"),
          width = 600,
          height = 600,
        ),
      ),
    )
  }
}
