package common

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

/** 
 * A an actor that also returns a value. It's a FutureFactory basically.
 * A call to ! will simply disregard the result. 
 */
trait WorkerActor[Msg, Result] extends SimpleActor[Msg] {
  protected def work(m: Msg): Result
  override protected def act(m: Msg) = work(m)
  def !!(m: Msg): Future[Result] = Future.apply(work(m))(ExecutionContext.fromExecutorService(queue)) 
}