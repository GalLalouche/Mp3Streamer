package mains.cover.image

import com.google.inject.Provides
import common.guice.ModuleUtils
import mains.cover.image.scrappa.ScrappaModule
import mains.cover.image.serp.SerpModule
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

private[cover] object ImageModule extends ScalaModule with ModuleUtils {
  override def configure(): Unit = {
    install(SerpModule)
    install(ScrappaModule)
  }

  // TODO another good question for SD!
  @Provides private def provideImageAPI(
      main: scrappa.API,
      fallback: serp.API,
      ec: ExecutionContext,
  ): ImageAPI = new FallbackImageAPI(main, fallback)(ec)
}
