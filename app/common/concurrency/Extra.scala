package common.concurrency

import scala.concurrent.Future

/** An actor with neither input nor output. Also provides overloads to avoid passing Unit explicitly. */
trait Extra extends SimpleActor[Unit] {
  final def !(): Future[Unit] = {this.!(())}
}
