package backend.configs

import java.time.Clock

trait ClockProvider {
  def clock: Clock
}
