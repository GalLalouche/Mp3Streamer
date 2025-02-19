package common.concurrency

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

import common.TimedLogger

class UpdatableProxyFactory @Inject() (timedLogger: TimedLogger) {
  def apply[A: Manifest](
      initialState: A,
      updateSelf: () => A,
  ): UpdatableProxy[A] = apply(initialState, updateSelf, manifest.runtimeClass.getSimpleName)
  def apply[A: Manifest](
      initialState: A,
      updateSelf: () => A,
      name: String,
  ): UpdatableProxy[A] = new UpdatableProxy[A](
    initialState,
    updateSelf,
    name,
    timedLogger,
  )

  def initialize[A: Manifest](
      updateSelf: () => A,
  )(implicit ec: ExecutionContext): Future[UpdatableProxy[A]] = Future(
    new UpdatableProxy[A](
      updateSelf(),
      updateSelf,
      manifest.runtimeClass.getSimpleName,
      timedLogger,
    ),
  )
}
