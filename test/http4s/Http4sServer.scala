package http4s

import javax.inject.Inject

import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.Port
import server.{RunningServer, Server}

import scala.concurrent.{ExecutionContext, Future}

import common.TimedLogger

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
