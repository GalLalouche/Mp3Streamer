package common.concurrency

import scala.concurrent.Future

/**
 * Represents a value that can be set using actor semantics, i.e., at most one set is active at any
 * given time.
 */
trait ActorState[Input, Output] {
  def set(a: Input): Future[Output]
  def get: Output
  def modify(f: Output => Output)(implicit ev: Output =:= Input): Future[Output] = set(ev(f(get)))
}
