package http4s

import javax.inject.Inject

import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.Port
import server.{RunningServer, Server}

import scala.concurrent.{ExecutionContext, Future}

import common.TimedLogger

class Http4sServer @Inject() (main: Main, ec: ExecutionContext, timedLogger: TimedLogger)
    extends Server {
  private implicit val iec: ExecutionContext = ec
  override def start(port: Int): Future[RunningServer] =
    Future {
      val resource = timedLogger("Creating main", println) {
        Main.run(main, Port.fromInt(port).get)
      }
      resource.allocated
    }.flatMap(_.unsafeToFuture())
      .map { case (_, shutdown) =>
        new RunningServer {
          override def stop(): Future[Unit] = shutdown.unsafeToFuture()
        }
      }
}
