package backend

import scala.concurrent.ExecutionContext

object StandaloneConfig extends RealConfig {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
}
