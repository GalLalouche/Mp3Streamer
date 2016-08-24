package controllers

import backend.RealConfig

import scala.concurrent.ExecutionContext

private object PlayConfig extends RealConfig {
  override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
}
