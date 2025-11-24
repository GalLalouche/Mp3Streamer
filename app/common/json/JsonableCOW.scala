package common.json

import scala.concurrent.Future

/**
 * {{{
 *  ___________________________
 * < Writes to JSON on changes >
 *  ---------------------------
 *         \   ^__^
 *          \  (oo)\_______
 *             (__)\       )\/\
 *                 ||----w |
 *                 ||     ||
 * }}}
 *
 * On [[set]], saves itself using `JsonableSaver`. On initial [[get]], returns the loaded value. Has
 * actor semantics, i.e., at most one [[set]] is active at any given time (hence the use of
 * [[Future]] in [[set]]).
 */
trait JsonableCOW[Input, Output] {
  def get: Output
  def set(a: Input): Future[Output]
  def modify(f: Output => Output)(implicit ev: Output =:= Input): Future[Output] = set(ev(f(get)))
}
