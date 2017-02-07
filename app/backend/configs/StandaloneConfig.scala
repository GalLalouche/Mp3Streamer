package backend.configs

import backend.logging.{ConsoleLogger, FilteringLogger, Logger}

import scala.concurrent.ExecutionContext

object StandaloneConfig extends RealConfig {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override implicit val logger: Logger = new ConsoleLogger with FilteringLogger
}
