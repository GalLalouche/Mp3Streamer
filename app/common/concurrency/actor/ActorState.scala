package common.concurrency.actor

import scala.concurrent.Future

/**
 * Represents a value that can be set using actor semantics, i.e., at most one set is active at any
 * given time. Why not just use an [[java.util.concurrent.atomic.AtomicReference]]? You'll notice
 * that the `input` and `output` types don't necessarily have to be the same, but also, setting a
 * value can have additional effects, or heavy computation, before the output is available.
 *
 * The object can always return the latest *stable* value, which isn't wrapped in a [[Future]]. Of
 * course, it's possible that generating the initial value involves a [[Future]], in which case look
 * to the [[UpdatableProxyFactory#initialize]] method.
 */
trait ActorState[Input, Output] {
  def set(a: Input): Future[Output]
  def get: Output
  def modify(f: Output => Output)(implicit ev: Output =:= Input): Future[Output] = set(ev(f(get)))
}
