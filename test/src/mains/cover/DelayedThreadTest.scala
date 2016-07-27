package mains.cover

import java.util.concurrent.{Semaphore, TimeUnit}

import com.google.common.base.Stopwatch
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FreeSpec, OneInstancePerTest}

class DelayedThreadTest extends FreeSpec with MockitoSugar with ShouldMatchers with OneInstancePerTest {
	private val $ = new DelayedThread("Name")
	private def waitFor(what: () => Boolean, timeInMillis: Long) {
		val sw = Stopwatch.createStarted()
		while (!what())
			if (sw.elapsed(TimeUnit.MILLISECONDS) > timeInMillis)
				fail("Timeout")
	}
	"DelayedThread" - {
		"Start should" - {
			"only be invokable once" in {
				testNames
				def start = $.start(() => ())
				start
				evaluating(start) should produce[IllegalStateException]
			}
			"be repeatable" in {
				var i = 0
				$.start(() => i += 1)
				val startingTime = System.currentTimeMillis()
				waitFor(() => i > 10, 1000)
			}
			"be interruptable" in {
				var i = 0
				$.start(() => i += 1)
				$.close()
				i = 0
				Thread.sleep(10)
				i should be === 0
			}
			"be interruptable even while blocking" in {
				val lock = new Semaphore(0)
				$.start(() => {
					lock.release(1);
					lock.acquire()
				})
				lock.acquire()
				waitFor(() => lock.hasQueuedThreads(), 1000)
				$.close()
				waitFor(() => !lock.hasQueuedThreads(), 1000)
			}
		}
	}

}
