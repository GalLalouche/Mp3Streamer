package backend
import models.MusicFinder
import slick.driver.{H2Driver, JdbcProfile}

import scala.concurrent.ExecutionContext

object TestConfiguration extends Configuration {
  override implicit val ec: ExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = ???
    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  override implicit val driver: JdbcProfile = H2Driver
  override implicit val db  = ???
  override implicit val mf: MusicFinder = ???
}
