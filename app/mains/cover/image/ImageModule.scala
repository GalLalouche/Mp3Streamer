package mains.cover.image

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

import common.guice.ModuleUtils

private[cover] object ImageModule extends ScalaModule with ModuleUtils {
  // TODO another good question for SD!
  @Provides private def provideImageAPI(
      main: serp.API,
      fallback: scrappa.API,
      ec: ExecutionContext,
  ): ImageAPI = new FallbackImageAPI(main, fallback)(ec)
}
