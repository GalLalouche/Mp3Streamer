package common.concurrency

import javax.inject.Inject

import common.TimedLogger

class UpdatableProxyFactory @Inject()(timedLogger: TimedLogger) {
  def apply[A: Manifest](
      initialState: A,
      updateSelf: () => A,
  ): UpdatableProxy[A] = new UpdatableProxy[A](
    initialState,
    updateSelf,
    manifest.runtimeClass.getSimpleName,
    timedLogger,
  )
}