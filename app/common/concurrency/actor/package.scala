package common.concurrency

import scala.concurrent.Future

package object actor {
  type ActorFunction[Msg, Result] = (Actor[Msg, Result], Msg) => Future[Result]
}
