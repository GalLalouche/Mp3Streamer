package server

import scala.concurrent.Future

trait Server {
  def start(port: Int): Future[RunningServer]
}
