package backend.configs

import backend.logging._

import scala.concurrent.ExecutionContext

object StandaloneConfig extends RealConfig {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  override implicit val logger = new ConsoleLogger with FilteringLogger
}
