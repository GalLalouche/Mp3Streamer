package mains.cover.image

import backend.module.TestModuleConfiguration
import com.google.inject.{Guice, Module}
import io.lemonlabs.uri.Url
import mains.cover.UrlSource
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import org.scalatest.freespec.AsyncFreeSpec
import play.api.libs.json.JsValue

import common.guice.RichModule.richModule
import common.rich.RichT.lazyT
import common.test.AuxSpecs

abstract class ImageApiSourceTest(
    module: Module,
    json: JsValue,
) extends AsyncFreeSpec
    with AuxSpecs {
  private val injector =
    Guice.createInjector(
      TestModuleConfiguration(_urlToBytesMapper = json.toString.getBytes.partialConst).module
        .overrideWith(module),
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
