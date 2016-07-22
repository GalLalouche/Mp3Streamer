package backend

import models.MusicFinder
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

trait Configuration {
  implicit val ec: ExecutionContext
  implicit val driver: JdbcProfile
  implicit val db: driver.backend.DatabaseDef
  implicit val mf: MusicFinder
}
