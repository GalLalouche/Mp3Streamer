package common

import akka.actor.Actor
import java.util.concurrent.Executors

object DaemonRunner {
	private val ex = Executors.newFixedThreadPool(1)

	def run(f: () => Unit) { ex.execute(new Runnable() { override def run { f() } }) }
}