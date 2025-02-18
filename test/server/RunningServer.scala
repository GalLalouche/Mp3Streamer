package server

import scala.concurrent.Future

trait RunningServer {
  def stop(): Future[Unit]
}
