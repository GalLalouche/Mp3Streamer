package common.concurrency.actor

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.ScalaDurationOps

import common.rich.primitives.RichInt.Rich

private class ExponentialBackoffer[Msg, Result](
    actor: Actor[Msg, Result],
    maxRetries: Int,
    duration: FiniteDuration,
    errorListener: Throwable => Unit,
) extends ActorTemplate[Msg, Result](s"${actor.name} Retrier") {
  override def !(m: => Msg): Future[Result] = {
    lazy val msg = m
    runMult(msg, 0, cause = null)
  }

  private def runMult(_msg: => Msg, attemptNumber: Int, cause: Exception): Future[Result] = {
    lazy val msg = _msg
    if (attemptNumber > maxRetries)
      Future.failed(new Exception(s"Failed all $maxRetries retries", cause.ensuring(_ != null)))
    else
      runOnce(msg, attemptNumber).recoverWith { case e =>
        errorListener(e)
        val newException = new Exception(s"Failed attempt #$attemptNumber", e)
        scribe.debug(s"Retrying after <${e.getMessage}> failure")
        runMult(msg, attemptNumber + 1, newException)
      }
  }

  private def runOnce(msg: => Msg, attemptNumber: Int): Future[Result] = {
    if (attemptNumber > 0) {
      assert(attemptNumber <= maxRetries)
      val coefficient = 2.exp(attemptNumber).toInt
      val sleepTime: FiniteDuration = coefficient * duration
      scribe.debug(s"Sleeping for $sleepTime before retrying")
      Thread.sleep(sleepTime.toJava)
    }
    actor ! msg
  }
}
