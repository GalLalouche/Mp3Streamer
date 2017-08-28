package backend.configs

import java.io.ByteArrayInputStream
import java.net.HttpURLConnection

import backend.Url
import backend.logging.{Logger, StringBuilderLogger}
import common.FakeClock
import common.io.MemoryRoot
import common.rich.RichT._

import scala.concurrent.ExecutionContext

// It's a case class so its copy constructor could be used by clients in order to configure it.
case class TestConfiguration(private val _ec: ExecutionContext = new ExecutionContext {
                               override def reportFailure(cause: Throwable): Unit = ???
                               override def execute(runnable: Runnable): Unit = runnable.run()
                             },
                             private val _mf: FakeMusicFinder = null,
                             private val _urlToBytesMapper: Url => Array[Byte] = _ => ???,
                             private val _httpTransformer: HttpURLConnection => HttpURLConnection = identity,
                             private val _root: MemoryRoot = new MemoryRoot)
    extends NonPersistentConfig {
  override implicit lazy val db: driver.backend.DatabaseDef =
    driver.api.Database.forURL(s"jdbc:h2:mem:test${System.identityHashCode(this)};DB_CLOSE_DELAY=-1", driver = "org.H2.JDBC")
  override implicit val ec: ExecutionContext = _ec
  override implicit val mf: FakeMusicFinder = _mf.opt.getOrElse(new FakeMusicFinder(_root))
  override def connection(u: Url) = {
    val $ = new HttpURLConnection(u.toURL) {
      override def disconnect() = ???
      override def usingProxy() = ???
      override def connect() = ???
      override def getInputStream = {
        new ByteArrayInputStream(_urlToBytesMapper(u))
      }
    }
    _httpTransformer($)
  }
  override implicit val logger: Logger = new StringBuilderLogger(new StringBuilder)
  override implicit lazy val rootDirectory: MemoryRoot = _root
  override implicit val clock: FakeClock = new FakeClock
}
