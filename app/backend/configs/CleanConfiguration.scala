package backend.configs

import backend.external.SlickExternalStorage
import backend.recon.{Album, AlbumReconStorage, Artist, ArtistReconStorage}
import common.io.{DirectoryRef, MemoryRoot}
import slick.driver.{H2Driver, JdbcProfile}

import scala.concurrent.ExecutionContext

/** No persistency */
object CleanConfiguration extends RealConfig {
  override lazy implicit val driver: JdbcProfile = H2Driver
  override implicit val db: driver.backend.DatabaseDef =
    driver.api.Database.forURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.H2.JDBC")
  override implicit val ec: ExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    override def execute(runnable: Runnable): Unit = runnable.run()
  }
  private def createTables() {
    implicit val c = CleanConfiguration
    new ArtistReconStorage().utils.createTable()
    new AlbumReconStorage().utils.createTable()
    new SlickExternalStorage[Artist]().utils.createTable()
    new SlickExternalStorage[Album]().utils.createTable()
  }
  createTables()
  override implicit lazy val rootDirectory: DirectoryRef = new MemoryRoot
}

