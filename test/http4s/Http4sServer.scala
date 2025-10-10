package http4s

import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.Port
import com.google.inject.Inject
import common.TimedLogger
import scala.concurrent.{ExecutionContext, Future}
import server.{RunningServer, Server}

private class Http4sServer @Inject() (main: Main, ec: ExecutionContext, timedLogger: TimedLogger)
    extends Server {
  private implicit val iec: ExecutionContext = ec
  override def start(port: Int): Future[RunningServer] = {
    val resource = timedLogger("Creating main", println) {
      Main.run(main, Port.fromInt(port).get)
    }
    resource.allocated.unsafeToFuture().map(_._2).map(shutdown => () => shutdown.unsafeToFuture())
  }
}
